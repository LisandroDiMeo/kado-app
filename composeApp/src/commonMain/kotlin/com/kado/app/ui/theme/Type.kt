package com.kado.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kado.composeapp.generated.resources.Res
import kado.composeapp.generated.resources.mplus1code_Bold
import kado.composeapp.generated.resources.mplus1code_ExtraLight
import kado.composeapp.generated.resources.mplus1code_Light
import kado.composeapp.generated.resources.mplus1code_Medium
import kado.composeapp.generated.resources.mplus1code_Regular
import kado.composeapp.generated.resources.mplus1code_SemiBold
import kado.composeapp.generated.resources.mplus1code_Thin
import org.jetbrains.compose.resources.Font

@Composable
fun MPlus1CodeFontFamily() = FontFamily(
    Font(Res.font.mplus1code_Thin, FontWeight.Thin),
    Font(Res.font.mplus1code_ExtraLight, FontWeight.ExtraLight),
    Font(Res.font.mplus1code_Light, FontWeight.Light),
    Font(Res.font.mplus1code_Regular, FontWeight.Normal),
    Font(Res.font.mplus1code_Medium, FontWeight.Medium),
    Font(Res.font.mplus1code_SemiBold, FontWeight.SemiBold),
    Font(Res.font.mplus1code_Bold, FontWeight.Bold),
)

private fun TextUnit.scaled(factor: Float): TextUnit =
    if (factor == 1.0f) this else (this.value * factor).sp

@Composable
fun AppTypography(scaleFactor: Float = 1.0f): Typography {
    val fontFamily = MPlus1CodeFontFamily()
    val baseline = Typography()
    return Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = fontFamily, fontSize = baseline.displayLarge.fontSize.scaled(scaleFactor)),
        displayMedium = baseline.displayMedium.copy(fontFamily = fontFamily, fontSize = baseline.displayMedium.fontSize.scaled(scaleFactor)),
        displaySmall = baseline.displaySmall.copy(fontFamily = fontFamily, fontSize = baseline.displaySmall.fontSize.scaled(scaleFactor)),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = fontFamily, fontSize = baseline.headlineLarge.fontSize.scaled(scaleFactor)),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = fontFamily, fontSize = baseline.headlineMedium.fontSize.scaled(scaleFactor)),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = fontFamily, fontSize = baseline.headlineSmall.fontSize.scaled(scaleFactor)),
        titleLarge = baseline.titleLarge.copy(fontFamily = fontFamily, fontSize = baseline.titleLarge.fontSize.scaled(scaleFactor)),
        titleMedium = baseline.titleMedium.copy(fontFamily = fontFamily, fontSize = baseline.titleMedium.fontSize.scaled(scaleFactor)),
        titleSmall = baseline.titleSmall.copy(fontFamily = fontFamily, fontSize = baseline.titleSmall.fontSize.scaled(scaleFactor)),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = fontFamily, fontSize = baseline.bodyLarge.fontSize.scaled(scaleFactor)),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = fontFamily, fontSize = baseline.bodyMedium.fontSize.scaled(scaleFactor)),
        bodySmall = baseline.bodySmall.copy(fontFamily = fontFamily, fontSize = baseline.bodySmall.fontSize.scaled(scaleFactor)),
        labelLarge = baseline.labelLarge.copy(fontFamily = fontFamily, fontSize = baseline.labelLarge.fontSize.scaled(scaleFactor)),
        labelMedium = baseline.labelMedium.copy(fontFamily = fontFamily, fontSize = baseline.labelMedium.fontSize.scaled(scaleFactor)),
        labelSmall = baseline.labelSmall.copy(fontFamily = fontFamily, fontSize = baseline.labelSmall.fontSize.scaled(scaleFactor)),
    )
}
