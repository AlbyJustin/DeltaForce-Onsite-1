package com.example.paperio

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.paperio.ui.theme.PaperIOTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaperIOTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DrawingCanvas(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DrawingCanvas(modifier: Modifier = Modifier) {
    var selectedColor by remember { mutableStateOf(Color.Red) }
    val points = remember { mutableStateListOf<Offset>() }
    var isPathClosed by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0f) }

    Column {
        ColorPicker(modifier, selectedColor) {
            selectedColor = it
        }
        Text(
            text = "Score: ${score.toInt()}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
        )
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            points.clear()
                            isPathClosed = false
                            points.add(offset)
                        },
                        onDrag = { change, _ ->
                            points.add(change.position)
                        },
                        onDragEnd = {
                            if (points.size > 2) {
                                isPathClosed = true
                                val area = calculatePolygonArea(points)
                                score = area / 1000

                            }
                        }
                    )
                }
        ) {
            if (points.size > 1) {
                val path = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (point in points.drop(1)) {
                        lineTo(point.x, point.y)
                    }
                    if (isPathClosed) close()
                }

                drawPath(
                    path = path,
                    color = selectedColor,
                    style = if (isPathClosed) Fill else Stroke(width = 6f)
                )
            }
        }
    }
}


val colorOptions = listOf(
    Color.Red, Color.Green, Color.Blue, Color.Yellow,
    Color.Magenta, Color.Cyan, Color.Black, Color.Gray
)

@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        colorOptions.forEach { color ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color, CircleShape)
                    .border(
                        width = if (color == selectedColor) 4.dp else 2.dp,
                        color = if (color == selectedColor) Color.White else Color.LightGray,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}

fun calculatePolygonArea(points: List<Offset>): Float {
    var area = 0f
    for (i in 0 until points.size - 1) {
        val x1 = points[i].x
        val x2 = points[i+1].x
        val y1 = points[i].y
        val y2 = points[i+1].y
        area += x1 * y2 - x2 * y1
    }
    area += points.last().x * points.first().y - points.first().x * points.last().y
    return kotlin.math.abs(area / 2)
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PaperIOTheme {
        DrawingCanvas()
    }
}