package com.example.game.screenmodels

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.game.models.GameMessage
import com.example.game.models.SensorData
import com.example.game.util.SensorManagerUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.random.Random

// Connection state to track the status of connections
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

// Game state to track the current game status
enum class GameState {
    NOT_STARTED,
    IN_PROGRESS,
    GAME_OVER
}

// Game turn to track whose turn it is to send a value
enum class GameTurn {
    HOST_TURN,
    CLIENT_TURN
}

// Sound directions based on random value
enum class SoundDirection(val value: Int, val displayName: String) {
    LEFT(0, "left"),
    MID(1, "mid"),
    RIGHT(2, "right");

    companion object {
        fun fromValue(value: Int): SoundDirection {
            return when (value) {
                0 -> LEFT
                1 -> MID
                2 -> RIGHT
                else -> throw IllegalArgumentException("Invalid value: $value")
            }
        }
    }
}

class GameScreenModel(private val context: Context) : ScreenModel {
    private val _messages = MutableStateFlow<List<GameMessage>>(emptyList())
    val messages: StateFlow<List<GameMessage>> = _messages.asStateFlow()

    private val _availableHosts = MutableStateFlow<List<NsdServiceInfo>>(emptyList())
    val availableHosts: StateFlow<List<NsdServiceInfo>> = _availableHosts.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // Connection state flow to track connection status
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // Error message flow
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Game state flows
    private val _gameState = MutableStateFlow(GameState.NOT_STARTED)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _gameTurn = MutableStateFlow(GameTurn.CLIENT_TURN)
    val gameTurn: StateFlow<GameTurn> = _gameTurn.asStateFlow()

    // Current sound direction for the host to play
    private val _currentSoundDirection = MutableStateFlow<SoundDirection?>(null)
    val currentSoundDirection: StateFlow<SoundDirection?> = _currentSoundDirection.asStateFlow()

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    private var nsdManager: NsdManager? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var registrationListener: NsdManager.RegistrationListener? = null

    private val SERVICE_NAME = "Game_${UUID.randomUUID()}"
    private val SERVICE_TYPE = "_game._tcp."
    private var username = "User_${(1000..9999).random()}"

    // Start hosting a game server
    fun startHosting(context: Context) {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(0)
                val port = serverSocket!!.localPort

                registerService(context, port)

                // Wait for a connection
                while (true) {
                    val socket = serverSocket!!.accept()
                    handleClientConnection(socket)
                    break  // For simplicity, only handle one client
                }
            } catch (e: Exception) {
                Log.e("GameScreenModel", "Error starting server", e)
            }
        }
    }

    private fun registerService(context: Context, port: Int) {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = SERVICE_NAME
            serviceType = SERVICE_TYPE
            setPort(port)
        }

        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(service: NsdServiceInfo) {
                Log.d("GameScreenModel", "Service registered: ${service.serviceName}")
            }

            override fun onRegistrationFailed(service: NsdServiceInfo, errorCode: Int) {
                Log.e("GameScreenModel", "Registration failed: $errorCode")
            }

            override fun onServiceUnregistered(service: NsdServiceInfo) {
                Log.d("GameScreenModel", "Service unregistered")
            }

            override fun onUnregistrationFailed(service: NsdServiceInfo, errorCode: Int) {
                Log.e("GameScreenModel", "Unregistration failed: $errorCode")
            }
        }

        nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    private fun handleClientConnection(socket: Socket) {
        clientSocket = socket
        reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        writer = PrintWriter(socket.getOutputStream(), true)

        // Update connection state and start the game
        screenModelScope.launch(Dispatchers.Main) {
            _connectionState.value = ConnectionState.Connected
            _gameState.value = GameState.IN_PROGRESS
            _gameTurn.value = GameTurn.CLIENT_TURN
        }

        // Start reading messages
        screenModelScope.launch(Dispatchers.IO) {
            try {
                var line: String?
                while (reader?.readLine().also { line = it } != null) {
                    val parts = line?.split(":", limit = 2)
                    if (parts?.size == 2) {
                        val sender = parts[0]
                        val content = parts[1]

                        withContext(Dispatchers.Main) {
                            _messages.value = _messages.value + GameMessage(
                                content = content,
                                senderName = sender,
                                isFromMe = false
                            )

                            // Process the received value for game logic if it's a number
                            try {
                                val value = content.toInt()
                                handleReceivedGameValue(value)
                            } catch (e: NumberFormatException) {
                                // Not a number, just a regular message
                                Log.d("GameScreenModel", "Received non-number message: $content")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GameScreenModel", "Error reading from client", e)
            }
        }
    }

    // Start discovering available hosts
    fun startDiscovery(context: Context) {
        stopDiscovery()
        _isScanning.value = true
        _availableHosts.value = emptyList()

        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e("GameScreenModel", "Discovery start failed: $errorCode")
                _isScanning.value = false
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e("GameScreenModel", "Discovery stop failed: $errorCode")
            }

            override fun onDiscoveryStarted(serviceType: String) {
                Log.d("GameScreenModel", "Discovery started")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d("GameScreenModel", "Discovery stopped")
                _isScanning.value = false
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d("GameScreenModel", "Service found: ${serviceInfo.serviceName}")
                if (serviceInfo.serviceType == SERVICE_TYPE) {
                    // Create a new ResolveListener for each service found
                    val resolveListener = object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                            Log.e("GameScreenModel", "Resolve failed: $errorCode")
                        }

                        override fun onServiceResolved(resolvedService: NsdServiceInfo) {
                            Log.d(
                                "GameScreenModel",
                                "Service resolved: ${resolvedService.serviceName}"
                            )
                            // Run on main thread to update UI safely
                            screenModelScope.launch(Dispatchers.Main) {
                                _availableHosts.value = _availableHosts.value + resolvedService
                            }
                        }
                    }

                    try {
                        nsdManager?.resolveService(serviceInfo, resolveListener)
                    } catch (e: Exception) {
                        Log.e("GameScreenModel", "Error resolving service", e)
                    }
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d("GameScreenModel", "Service lost: ${serviceInfo.serviceName}")
                screenModelScope.launch(Dispatchers.Main) {
                    _availableHosts.value = _availableHosts.value.filter {
                        it.serviceName != serviceInfo.serviceName
                    }
                }
            }
        }

        nsdManager?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun stopDiscovery() {
        discoveryListener?.let {
            try {
                nsdManager?.stopServiceDiscovery(it)
            } catch (e: Exception) {
                Log.e("GameScreenModel", "Error stopping discovery", e)
            }
        }
        discoveryListener = null
        _isScanning.value = false
    }

    // Connect to a host
    fun connectToHost(serviceInfo: NsdServiceInfo) {
        Log.d(
            "GameScreenModel",
            "Connecting to host: ${serviceInfo.serviceName} at ${serviceInfo.host}:${serviceInfo.port}"
        )

        // Update state to connecting
        screenModelScope.launch(Dispatchers.Main) {
            _connectionState.value = ConnectionState.Connecting
            _errorMessage.value = null
        }

        screenModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(
                    "GameScreenModel",
                    "Attempting socket connection to ${serviceInfo.host}:${serviceInfo.port}"
                )
                val socket = Socket(serviceInfo.host, serviceInfo.port)
                clientSocket = socket
                reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                writer = PrintWriter(socket.getOutputStream(), true)

                // Update state to connected
                withContext(Dispatchers.Main) {
                    _connectionState.value = ConnectionState.Connected
                    _gameState.value = GameState.IN_PROGRESS
                    _gameTurn.value = GameTurn.CLIENT_TURN
                    Log.d("GameScreenModel", "Successfully connected to host")

                    // Auto-send initial random value after connecting
                    sendRandomValue()
                }

                // Start reading messages
                try {
                    var line: String?
                    Log.d("GameScreenModel", "Starting to read messages from server")
                    while (reader?.readLine().also { line = it } != null) {
                        Log.d("GameScreenModel", "Received message: $line")
                        val parts = line?.split(":", limit = 2)
                        if (parts?.size == 2) {
                            val sender = parts[0]
                            val content = parts[1]

                            withContext(Dispatchers.Main) {
                                _messages.value = _messages.value + GameMessage(
                                    content = content,
                                    senderName = sender,
                                    isFromMe = false
                                )

                                // Process the received value for game logic if it's a number
                                try {
                                    val value = content.toInt()
                                    handleReceivedGameValue(value)
                                } catch (e: NumberFormatException) {
                                    // Not a number, just a regular message
                                    Log.d(
                                        "GameScreenModel",
                                        "Received non-number message: $content"
                                    )
                                }
                            }
                        }
                    }

                    // If we exit the loop normally, the connection was closed by the server
                    withContext(Dispatchers.Main) {
                        _connectionState.value = ConnectionState.Disconnected
                        _errorMessage.value = "Server closed the connection"
                    }

                } catch (e: Exception) {
                    Log.e("GameScreenModel", "Error reading from server", e)
                    withContext(Dispatchers.Main) {
                        _connectionState.value =
                            ConnectionState.Error("Error reading from server: ${e.message}")
                        _errorMessage.value = "Connection error: ${e.message}"
                    }
                }

            } catch (e: Exception) {
                Log.e("GameScreenModel", "Error connecting to host", e)
                withContext(Dispatchers.Main) {
                    _connectionState.value =
                        ConnectionState.Error("Failed to connect: ${e.message}")
                    _errorMessage.value = "Failed to connect: ${e.message}"
                }
            }
        }
    }

    // Send a random value (0, 1, or 2)
    fun sendRandomValue() {
        if (_gameState.value == GameState.IN_PROGRESS) {
            val isClientTurn = _gameTurn.value == GameTurn.CLIENT_TURN
            val isHost = serverSocket != null

            // Only allow sending if it's the sender's turn
            if ((isHost && !isClientTurn) || (!isHost && isClientTurn)) {
                screenModelScope.launch(Dispatchers.IO) {
                    try {
                        val randomValue = Random.nextInt(0, 3)
                        writer?.println("$username:$randomValue")

                        withContext(Dispatchers.Main) {
                            _messages.value = _messages.value + GameMessage(
                                content = randomValue.toString(),
                                senderName = username,
                                isFromMe = true
                            )

                            // Switch turns after sending
                            _gameTurn.value = if (_gameTurn.value == GameTurn.CLIENT_TURN) {
                                GameTurn.HOST_TURN
                            } else {
                                GameTurn.CLIENT_TURN
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("GameScreenModel", "Error sending message", e)
                    }
                }
            } else {
                Log.d("GameScreenModel", "Not your turn to send a value")
            }
        } else {
            Log.d("GameScreenModel", "Game not in progress")
        }
    }

    // Start automatic game exchange - called when a client connects to host
    private fun startAutomaticGameExchange() {
        screenModelScope.launch {
            // Client automatically sends first value upon connection
            // This is handled in the connectToHost method
            Log.d("GameScreenModel", "Starting automatic game exchange")
        }
    }

    private fun handleReceivedGameValue(value: Int) {
        if (_gameState.value != GameState.IN_PROGRESS) return

        val isHost = serverSocket != null
        val isClientTurn = _gameTurn.value == GameTurn.CLIENT_TURN

        // Only process if it's the receiver's turn
        if ((isHost && isClientTurn) || (!isHost && !isClientTurn)) {
            Log.d("GameScreenModel", "Processing received value: $value")

            // Switch turns now that we've received a value
            // Switch turns now that we've received a value
            _gameTurn.value = if (isClientTurn) GameTurn.HOST_TURN else GameTurn.CLIENT_TURN

            if (isHost) {
                // Host received a value from client
                // Process the value and prepare to play sound after delay
                try {
                    val soundDirection = SoundDirection.fromValue(value)

                    // Update the current sound direction for UI to display
                    _currentSoundDirection.value = soundDirection

                    // Simulate 1-second delay before "playing" the sound
                    screenModelScope.launch {
                        // Add a message indicating the delay
                        _messages.value = _messages.value + GameMessage(
                            content = "Preparing sound: ${soundDirection.displayName}...",
                            senderName = "System",
                            isFromMe = true
                        )

                        // 1-second delay
                        kotlinx.coroutines.delay(1000)

                        // After delay, "play" the sound (just update UI for now)
                        _messages.value = _messages.value + GameMessage(
                            content = "Playing sound: ${soundDirection.displayName}",
                            senderName = "System",
                            isFromMe = true
                        )

                        // After "playing" the sound, wait a moment then automatically send response value
                        kotlinx.coroutines.delay(2000) // Wait 2 seconds to simulate user response time

                        // Auto-send random value as host's response
                        if (_gameState.value == GameState.IN_PROGRESS) {
                            sendRandomValue()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GameScreenModel", "Error processing value for sound", e)
                }
            } else {
                // Client received a value from host
                // Process the value (client-side logic)
                try {
                    val soundDirection = SoundDirection.fromValue(value)

                    // Add a message indicating what value was received
                    _messages.value = _messages.value + GameMessage(
                        content = "Received direction: ${soundDirection.displayName}",
                        senderName = "System",
                        isFromMe = true
                    )

                    // Client automatically responds after a short delay
                    screenModelScope.launch {
                        kotlinx.coroutines.delay(1500) // Wait 1.5 seconds to simulate thinking time

                        // Auto-send random value as client's response
                        if (_gameState.value == GameState.IN_PROGRESS) {
                            sendRandomValue()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GameScreenModel", "Error processing received value on client", e)
                }
            }
            Log.d("GameScreenModel", "Received value out of turn, ignoring")
        }
    }

    // Send a text message
    fun sendMessage(message: String) {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                if (writer == null) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "Not connected to any host"
                    }
                    return@launch
                }

                writer?.println("$username:$message")

                withContext(Dispatchers.Main) {
                    _messages.value = _messages.value + GameMessage(
                        content = message,
                        senderName = username,
                        isFromMe = true
                    )
                }
            } catch (e: Exception) {
                Log.e("GameScreenModel", "Error sending message", e)
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Failed to send message: ${e.message}"
                }
            }
        }
    }

    // Reset error message
    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    // Disconnect from host
    fun disconnect() {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                writer?.close()
                reader?.close()
                clientSocket?.close()

                withContext(Dispatchers.Main) {
                    _connectionState.value = ConnectionState.Disconnected
                }
            } catch (e: Exception) {
                Log.e("GameScreenModel", "Error disconnecting", e)
                withContext(Dispatchers.Main) {
                    _connectionState.value =
                        ConnectionState.Error("Error disconnecting: ${e.message}")
                    _errorMessage.value = "Error disconnecting: ${e.message}"
                }
            }
        }
    }

    // Check if this instance is being used as a host
    fun isHost(): Boolean {
        return serverSocket != null
    }

    override fun onDispose() {
        super.onDispose()
        // Clean up resources
        stopDiscovery()
        stopDiscovery()

        registrationListener?.let {
            try {
                nsdManager?.unregisterService(it)
            } catch (e: Exception) {
                Log.e("GameScreenModel", "Error unregistering service", e)
            }
        }
        stopRecording()
        sensorManagerUtil.unregisterListeners()

        try {
            writer?.close()
            reader?.close()
            clientSocket?.close()
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e("GameScreenModel", "Error cleaning up resources", e)
        }

    }


    // old impl

    private val sensorManagerUtil = SensorManagerUtil(context)
    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private var fileWriter: FileWriter? = null

    fun toggleRecording() {
        if (_isRecording.value) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    //manish
    fun toggleRecording(onRecordingStopped: ((List<SensorData>) -> Unit)? = null) {
        if (_isRecording.value) {
            stopRecording(onRecordingStopped)
        } else {
            startRecording()
        }
    }
//manish

    private fun startRecording() {
        try {
            val file = File(
                context.getExternalFilesDir(null),
                "sensor_data.csv"
            )
            fileWriter = FileWriter(file, false).apply {
                write(SensorData.getCsvHeader() + "\n")
            }
            _isRecording.value = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

//    private fun stopRecording() {
//        try {
//            fileWriter?.close()
//
//            val file = File(context.getExternalFilesDir(null), "sensor_data.csv")
//            val recordedData = file.readLines().drop(1) // Drop the header row
//
//            val downsampledData = downsampleDataTo100Rows(recordedData)
//
//            FileWriter(file, false).use { writer ->
//                writer.write(SensorData.getCsvHeader() + "\n") // Write the header
//                downsampledData.forEach { row ->
//                    writer.write(row + "\n")
//                }
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        } finally {
//            fileWriter = null
//            _isRecording.value = false
//        }
//    }

    //manish
    fun stopRecording(onRecordingStopped: ((List<SensorData>) -> Unit)? = null) {
        try {
            fileWriter?.close()

            val file = File(context.getExternalFilesDir(null), "sensor_data.csv")
            val recordedData = file.readLines().drop(1)

            val downsampledData = downsampleDataTo100Rows(recordedData)

            FileWriter(file, false).use { writer ->
                writer.write(SensorData.getCsvHeader() + "\n")
                downsampledData.forEach { row ->
                    writer.write(row + "\n")
                }
            }

            val sensorDataList = csvFileToSensorData(file)
            onRecordingStopped?.invoke(sensorDataList)

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileWriter = null
            _isRecording.value = false
        }
    }

    //manish
    private fun downsampleDataTo100Rows(data: List<String>): List<String> {
        val totalRows = data.size
        val step = if (totalRows > 100) totalRows / 100 else 1
        return data.filterIndexed { index, _ -> index % step == 0 }.take(100)
    }

    fun getSavedData(): List<SensorData> {
        val file = File(context.getExternalFilesDir(null), "sensor_data.csv")
        return csvFileToSensorData(file)
    }

    fun csvFileToSensorData(file: File): List<SensorData> {
        if (!file.exists()) return emptyList()

        try {
            // Read all lines and skip the header
            val lines = file.readLines().drop(1)

            return lines.mapNotNull { line ->
                try {
                    // Split the CSV row into values
                    val values = line.split(",")

                    // Check if we have enough values
                    if (values.size >= 6) {
                        // Parse the values into float arrays
                        val linearAcceleration = FloatArray(3) { i ->
                            values[i].toFloatOrNull() ?: 0f
                        }

                        val gyroscope = FloatArray(3) { i ->
                            values[i + 3].toFloatOrNull() ?: 0f
                        }

                        // Create magnetometer array if data exists
                        val magnetometer = if (values.size >= 9) {
                            FloatArray(3) { i ->
                                values[i + 6].toFloatOrNull() ?: 0f
                            }
                        } else {
                            FloatArray(3)
                        }

                        SensorData(linearAcceleration, gyroscope, magnetometer)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    init {
        sensorManagerUtil.setOnSensorDataChangedListener { updatedData ->
            _sensorData.value = updatedData
            if (isRecording.value) {
                try {
                    fileWriter?.write(updatedData.toCsvRow() + "\n")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
//
//    override fun onDispose() {
//        stopRecording()
//        sensorManagerUtil.unregisterListeners()
//    }
}
