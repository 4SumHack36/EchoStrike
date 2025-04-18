package com.foursum.helper.ui.screens


import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.edit


@Composable
fun SaveComponent(
    context: Context,
    sharedPrefsManager: SharedPrefsManager
) {

    var text by remember { mutableStateOf(sharedPrefsManager.getString("fileName")) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        currentPrefix = text
        n = sharedPrefsManager.getInt("n", 1)
        nextFileName = "${currentPrefix}_$n"
        Text(text = "Next File Name: $nextFileName")
        Text(text = "currentPrefix: $currentPrefix")
        Text(text = "n: $n")
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter File Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
        )
        var nVal by remember { mutableStateOf(sharedPrefsManager.getInt("n").toString()) }
        OutlinedTextField(
            value = nVal,
            onValueChange = { nVal = it },
            label = { Text("Enter the value of n") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Button(
            onClick = {
//                                handleIntent(intent)
                // save filename to sharedprefs
//                sharedPref.edit {
//                    putString("fileName", text)
//                }
                sharedPrefsManager.saveString("fileName", text)
                Toast.makeText(context, "File Name Saved", Toast.LENGTH_SHORT).show()
                currentPrefix = text
                nextFileName = "${currentPrefix}_$n"
            },
        ) {
            Text("Save Prefix")
        }
        Button(
            onClick = {
                if (nVal.isNotEmpty() && nVal.toIntOrNull() == null) {
                    Toast.makeText(context, "Number Daal Bhadwe", Toast.LENGTH_SHORT)
                        .show()
                    return@Button
                }
                n = nVal.toInt()
//                sharedPref.edit {
//                    putInt("n", n)
//                }
                sharedPrefsManager.saveInt("n", n)

                nextFileName = "${currentPrefix}_$n"
            }
        ) {
            Text("Save n")
        }
    }
}