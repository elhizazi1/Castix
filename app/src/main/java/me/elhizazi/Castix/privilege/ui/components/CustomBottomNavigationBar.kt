package me.elhizazi.Castix.privilege.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import me.elhizazi.Castix.privilege.ui.AppStrings

@Composable
fun CustomBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    lang: String,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        Triple(AppStrings.get("nav_home", lang), Icons.Default.Dashboard, Icons.Outlined.Dashboard),
        Triple(AppStrings.get("nav_amoled", lang), Icons.Default.Visibility, Icons.Outlined.Visibility),
        Triple(AppStrings.get("nav_engines", lang), Icons.Default.Tune, Icons.Outlined.Tune),
        Triple(AppStrings.get("nav_settings", lang), Icons.Default.Settings, Icons.Outlined.Settings)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        navItems.forEachIndexed { index, item ->
            val isSelected = selectedTab == index

            val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

            val weight by animateFloatAsState(
                targetValue = if (isSelected) 2.2f else 1f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                label = "weight"
            )

            val interactionSource = remember { MutableInteractionSource() }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(bgColor)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onTabSelected(index) }
                        )
                        .testTag("nav_item_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        AnimatedVisibility(
                            visible = isSelected && index >= 2,
                            enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(tween(200)),
                            exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut(tween(100))
                        ) {
                            Text(
                                text = item.first,
                                color = contentColor,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }

                        Icon(
                            imageVector = if (isSelected) item.second else item.third,
                            contentDescription = item.first,
                            tint = contentColor,
                            modifier = Modifier.size(26.dp)
                        )

                        AnimatedVisibility(
                            visible = isSelected && index < 2,
                            enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(tween(200)),
                            exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut(tween(100))
                        ) {
                            Text(
                                text = item.first,
                                color = contentColor,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
