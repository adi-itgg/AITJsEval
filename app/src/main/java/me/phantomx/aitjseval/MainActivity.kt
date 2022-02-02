package me.phantomx.aitjseval

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.phantomx.aitjseval.listener.OnJavaScriptResponseListener
import me.phantomx.aitjseval.ui.theme.AITJsEvalTheme

class MainActivity : ComponentActivity() {

    private val rText = mutableStateOf("Result")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PreviewView()
        }
    }

    @Composable
    fun ThisLayout() {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Example javascript
            val sc  = """
                (function f() {
                    var d = "EvalExample:10"
                    var i = parseInt(d.split(":")[1]) + 2 * 88 
                    return d.split(":")[0] + " " + i
                }());
            """
            Text(text = sc,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(32.dp)
            )
            val context = LocalContext.current
            Button(onClick = {
                // Run example eval javascript
                AITJsEval.get().enqueue("sample", sc, object : OnJavaScriptResponseListener {
                    override fun onResponse(script: Script) {
                        rText.value = script.result
                        Toast.makeText(context, "Script executed", Toast.LENGTH_SHORT).show()
                    }
                })
            }) {
                Text(text = "Run")
            }
            val t by rText
            Text(text = t,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(32.dp),
                textAlign = TextAlign.Center
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewView() {
        AITJsEvalTheme {
            ThisLayout()
        }
    }
}