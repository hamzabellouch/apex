package com.tkno.blueiris.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tkno.blueiris.R

@Composable
fun OutlinedButtonWithIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = Color.Unspecified,
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
    ) {
        Icon(
            modifier = Modifier.size(ButtonDefaults.IconSize),
            imageVector = icon,
            contentDescription = null,
        )
        Text(modifier = Modifier.padding(start = 8.dp), text = text, color = textColor)
    }
}

@Composable
fun TextButtonWithIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier,
        onClick = onClick,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(modifier = Modifier.size(18.dp), imageVector = icon, contentDescription = null)
            Text(modifier = Modifier.padding(start = 8.dp), text = text)
        }
    }
}

@Composable
fun FilledTonalButtonWithIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    textColor: Color = Color.Unspecified,
) {
    FilledTonalButton(
        modifier = modifier,
        onClick = onClick,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        colors = colors,
    ) {
        Icon(modifier = Modifier.size(18.dp), imageVector = icon, contentDescription = null)
        Text(modifier = Modifier.padding(start = 8.dp), text = text, color = textColor)
    }
}

@Composable
fun FilledButtonWithIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        enabled = enabled,
    ) {
        Icon(modifier = Modifier.size(18.dp), imageVector = icon, contentDescription = null)
        Text(modifier = Modifier.padding(start = 6.dp), text = text)
    }
}

@Composable
fun ConfirmButton(
    text: String = stringResource(R.string.confirm),
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
    ) {
        Text(text)
    }
}

@Composable
fun DismissButton(text: String = stringResource(R.string.dismiss), onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors =
            ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
    ) {
        Text(text)
    }
}
