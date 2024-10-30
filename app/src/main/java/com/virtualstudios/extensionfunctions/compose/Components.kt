package com.virtualstudios.extensionfunctions.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.virtualstudios.extensionfunctions.R

@Composable
fun DisplayLargeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily(
            Font(
                resId = R.font.gilroy_bold
            )
        ),
        fontSize = 57.sp,
        lineHeight = 64.sp,
        modifier = modifier
    )
}

@Composable
fun DisplayMediumText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily(
            Font(
                resId = R.font.gilroy_bold
            )
        ),
        fontSize = 45.sp,
        lineHeight = 52.sp,
        modifier = modifier
    )
}

@Composable
fun DisplaySmallText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily(
            Font(
                resId = R.font.gilroy_bold
            )
        ),
        fontSize = 36.sp,
        lineHeight = 44.sp,
        modifier = modifier
    )
}

@Composable
fun HeadLineLargeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily(
            Font(
                resId = R.font.gilroy_bold
            )
        ),
        fontSize = 32.sp,
        lineHeight = 40.sp,
        modifier = modifier
    )
}

@Composable
fun HeadLineMediumText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily(
            Font(
                resId = R.font.gilroy_bold
            )
        ),
        fontSize = 28.sp,
        lineHeight = 36.sp,
        modifier = modifier
    )
}

@Composable
fun HeadLineSmallText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily(
            Font(
                resId = R.font.gilroy_bold
            )
        ),
        fontSize = 24.sp,
        lineHeight = 32.sp,
        modifier = modifier
    )
}

@Composable
fun TitleLargeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily(
            Font(
                resId = R.font.gilroy_medium
            )
        ),
        fontSize = 22.sp,
        lineHeight = 28.sp,
        modifier = modifier
    )
}

@Composable
fun TitleMediumText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily(
            Font(
                resId = R.font.gilroy_bold
            )
        ),
        fontSize = 16.sp,
        lineHeight = 24.sp,
        modifier = modifier
    )
}

@Composable
fun TitleSmallText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily(
            Font(
                resId = R.font.gilroy_bold
            )
        ),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        modifier = modifier
    )
}

@Composable
fun BodyLargeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily(
            Font(
                resId = R.font.gilroy_bold
            )
        ),
        fontSize = 16.sp,
        lineHeight = 24.sp,
        modifier = modifier
    )
}

@Composable
fun BodyMediumText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily(
            Font(
                resId = R.font.gilroy_bold
            )
        ),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        modifier = modifier
    )
}

@Composable
fun BodySmallText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily(
            Font(
                resId = R.font.gilroy_bold
            )
        ),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        modifier = modifier
    )
}

@Composable
fun LabelLargeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily(
            Font(
                resId = R.font.gilroy_medium
            )
        ),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        modifier = modifier
    )
}

@Composable
fun LabelMediumText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily(
            Font(
                resId = R.font.gilroy_medium
            )
        ),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        modifier = modifier
    )
}

@Composable
fun LabelSmallText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily(
            Font(
                resId = R.font.gilroy_medium
            )
        ),
        fontSize = 11.sp,
        lineHeight = 16.sp,
        modifier = modifier
    )
}