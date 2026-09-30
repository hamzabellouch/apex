package com.tkno.apex.ui.page.settings.about

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tkno.apex.R
import com.tkno.apex.theme.getAppTypography
import com.tkno.apex.ui.icon.Translate

private data class WhatsNewItem(
    val textEn: String,
    val textAr: String,
    @DrawableRes val beforeImage: Int? = null,
    @DrawableRes val afterImage: Int? = null,
    @DrawableRes val singleImage: Int? = null,
)

@Composable
fun WhatsNewDialog(
    onDismissRequest: () -> Unit,
    versionName: String = "0.1.0-beta",
    releaseDate: String = "September 30, 2026",
) {
    var isArabic by remember { mutableStateOf(false) }

    val formattedVersion = if (versionName.startsWith("v", ignoreCase = true)) versionName else "v$versionName"
    val dateText = if (isArabic) "30 سبتمبر 2026" else releaseDate
    val titleText = if (isArabic) "ما الجديد" else stringResource(R.string.whats_new)
    val highlightsTitleText = if (isArabic) "أحدث التحديثات والتحسينات" else stringResource(R.string.whats_new_highlights_title)

    val updateItems = listOf(
        WhatsNewItem(
            textEn = "Added an Info icon to explain system limitations after Stop and Clean operations",
            textAr = "إضافة أيقونة معلومات لشرح قيود النظام بعد عمليات الإيقاف والتنظيف"
        ),
        WhatsNewItem(
            textEn = "Show the Info icon only when remaining items exist and reset its visibility after app relaunch",
            textAr = "إظهار أيقونة المعلومات فقط عند وجود عناصر متبقية وإعادة تعيين ظهورها بعد إعادة تشغيل التطبيق"
        ),
        WhatsNewItem(
            textEn = "Added explanatory dialogs showing only the system limitation text without listing remaining apps",
            textAr = "إضافة نوافذ حوارية توضيحية تعرض نص قيود النظام فقط دون سرد التطبيقات المتبقية"
        ),
        WhatsNewItem(
            textEn = "Added a custom Report icon for problem reporting",
            textAr = "إضافة أيقونة إبلاغ مخصصة للإبلاغ عن المشكلات"
        ),
        WhatsNewItem(
            textEn = "Added localized strings for the problem reporting feature",
            textAr = "إضافة نصوص مترجمة لميزة الإبلاغ عن المشكلات"
        ),
        WhatsNewItem(
            textEn = "Added a Report Problem button inside the issue tracker card",
            textAr = "إضافة زر الإبلاغ عن مشكلة داخل بطاقة متتبع المشكلات"
        ),
        WhatsNewItem(
            textEn = "Integrated the Report Problem dialog with the updated Google Form",
            textAr = "دمج نافذة الإبلاغ عن مشكلة مع نموذج Google المحدّث"
        ),
        WhatsNewItem(
            textEn = "Created ManualOperationManager for manual app switching when Accessibility Service is unavailable",
            textAr = "إنشاء ManualOperationManager للتبديل اليدوي بين التطبيقات عند عدم توفر خدمة إمكانية الوصول"
        ),
        WhatsNewItem(
            textEn = "Added support for Manual, Automatic, and Ask Each Time modes for Stop and Clean actions",
            textAr = "إضافة دعم للأوضاع اليدوي والتلقائي والسؤال في كل مرة لإجراءات الإيقاف والتنظيف"
        ),
        WhatsNewItem(
            textEn = "Improved Picture-in-Picture handling and lifecycle cleanup for manual operations",
            textAr = "تحسين معالجة وضع صورة داخل صورة (PiP) وتنظيف دورة الحياة للعمليات اليدوية"
        ),
        WhatsNewItem(
            textEn = "Added multilingual dialogs for operation mode selection and manual process guidance",
            textAr = "إضافة نوافذ حوارية متعددة اللغات لاختيار وضع التشغيل والإرشاد للعملية اليدوية"
        )
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
        ) {
            MaterialTheme(
                typography = remember(isArabic) { getAppTypography(isArabic) }
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .heightIn(max = 640.dp)
                        .padding(vertical = 24.dp)
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header: Title, version/date, and Close 'X' Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$dateText | $formattedVersion",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cancel),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Scrollable Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Banner Image
                        Image(
                            painter = painterResource(id = R.drawable.whats_new_banner),
                            contentDescription = "What's New Banner",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1024f / 537f)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Section Title
                        Text(
                            text = highlightsTitleText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Update items list
                        updateItems.forEach { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 7.dp)
                                            .size(6.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (isArabic) item.textAr else item.textEn,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 20.sp
                                    )
                                }

                                if (item.beforeImage != null && item.afterImage != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = item.beforeImage),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(16f / 9f)
                                                .clip(RoundedCornerShape(10.dp))
                                        )

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )

                                        Image(
                                            painter = painterResource(id = item.afterImage),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(16f / 9f)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    }
                                } else if (item.singleImage != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = item.singleImage),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth(0.48f)
                                                .aspectRatio(16f / 9f)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Language Toggle Capsule Button (Arabic / English)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.clip(CircleShape)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .clickable(
                                            role = Role.Button,
                                            onClick = { isArabic = !isArabic }
                                        )
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Translate,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (isArabic) "English" else "Arabic",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
}


