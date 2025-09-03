package com.phuonghai.inspection.presentation.generalUI
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phuonghai.inspection.presentation.theme.SafetyYellow

@Composable
fun ButtonUI(
    modifier: Modifier = Modifier,
    text: String = "Next",
    backgroundColor: Color = SafetyYellow,
    textColor: Color = Color.White,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    fontSize: Int = 14,
    shape: Int = 10,
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = Color(0xFFB4A6A4), // Customize this
            disabledContentColor = Color.White),
        contentPadding = PaddingValues(horizontal = 30.dp, vertical = 10.dp),
        shape = RoundedCornerShape(shape.dp),
        enabled = enabled,

        ) {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = text,
                style = textStyle,
                fontSize = fontSize.sp
            )
            if(icon != null){
                Spacer(modifier = Modifier.width(10.dp))
                Icon(imageVector = icon, contentDescription = null, tint = Color.White)
            }
        }
    }
}