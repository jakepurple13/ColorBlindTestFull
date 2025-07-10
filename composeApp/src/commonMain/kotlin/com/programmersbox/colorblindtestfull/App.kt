package com.programmersbox.colorblindtestfull


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import colorblindtestfull.composeapp.generated.resources.Res
import colorblindtestfull.composeapp.generated.resources.compose_multiplatform
import colorblindtestfull.composeapp.generated.resources.ishihara_color_blindness_test
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        val colorFilter = remember {
            mutableStateListOf<Float>().apply {
                ColorBlindnessType.None.array.forEach {
                    add(it)
                }
            }
        }

        val clipboard = LocalClipboardManager.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Color Blindness Test") },
                    actions = {
                        IconButton(
                            onClick = {
                                clipboard.setText(
                                    AnnotatedString("floatArrayOf(${colorFilter.joinToString(",")})")
                                )
                            }
                        ) { Icon(Icons.Default.ImportExport, null) }
                    }
                )
            },
        ) { padding ->

            var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

            val scope = rememberCoroutineScope()

            val filePicker = rememberFilePickerLauncher(
                type = FileKitType.Image,
            ) { file ->
                scope.launch {
                    file
                        ?.readBytes()
                        ?.decodeToImageBitmap()
                        ?.let { bitmap = it }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(padding)
                    .padding(vertical = 4.dp)
                    .fillMaxSize(),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ColorBlindnessType.entries.forEach {
                            Button(
                                onClick = {
                                    colorFilter.clear()
                                    colorFilter.addAll(it.array.toList())
                                }
                            ) { Text(it.name) }
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth(.3f)
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { filePicker.launch() }
                    ) {
                        bitmap?.let {
                            Image(
                                it,
                                null,
                                colorFilter = ColorMatrixColorFilter(ColorMatrix(colorFilter.toFloatArray())),
                            )
                        } ?: Image(
                            painter = painterResource(Res.drawable.ishihara_color_blindness_test),
                            null,
                            colorFilter = ColorMatrixColorFilter(ColorMatrix(colorFilter.toFloatArray())),
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { filePicker.launch() }
                        ) { Text("Select Image") }
                        Button(
                            onClick = { bitmap = null }
                        ) { Text("Reset Image") }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(.5f)
                )

                Text("Values must be between 0.0 and 1.0")

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5)
                ) {
                    colorFilter
                        .toList()
                        .chunked(5)
                        .forEachIndexed { chunkIndex, list ->
                            itemsIndexed(list) { localIndex, value ->
                                val absoluteIndex = chunkIndex * 5 + localIndex
                                OutlinedTextField(
                                    value = value.toString(),
                                    onValueChange = {
                                        colorFilter[absoluteIndex] = it
                                            .toFloatOrNull()
                                            ?.coerceIn(0f, 1f)
                                            ?: value
                                    },
                                    prefix = {
                                        Text(
                                            when (localIndex) {
                                                0 -> "R"
                                                1 -> "G"
                                                2 -> "B"
                                                3 -> "A"
                                                else -> ""
                                            }
                                        )
                                    },
                                    suffix = { Text("F") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )
                            }
                        }
                }
            }
        }
    }
}

enum class ColorBlindnessType(
    val array: FloatArray,
) {
    None(
        floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    ),

    /**
     * Red-green color blindness (red weak/missing)
     */
    Protanopia(
        floatArrayOf(
            0.567F, 0.433F, 0.0F, 0.0F, 0.0F,
            0.558F, 0.442F, 0.0F, 0.0F, 0.0F,
            0.0F, 0.242F, 0.758F, 0.0F, 0.0F,
            0.0F, 0.0F, 0.0F, 1.0F, 0.0F
        )
    ),

    /**
     * Blue-yellow color blindness (blue weak/missing)
     */
    Deuteranopia(
        floatArrayOf(
            0.625F, 0.375F, 0.0F, 0.0F, 0.0F,
            0.7F, 0.3F, 0.0F, 0.0F, 0.0F,
            0.0F, 0.3F, 0.7F, 0.0F, 0.0F,
            0.0F, 0.0F, 0.0F, 1.0F, 0.0F
        )
    ),

    /**
     * Green-blue color blindness (green weak/missing)
     */
    Tritanopia(
        floatArrayOf(
            0.95F, 0.05F, 0.0F, 0.0F, 0.0F,
            0.0F, 0.433F, 0.567F, 0.0F, 0.0F,
            0.0F, 0.475F, 0.525F, 0.0F, 0.0F,
            0.0F, 0.0F, 0.0F, 1.0F, 0.0F
        )
    ),
}
