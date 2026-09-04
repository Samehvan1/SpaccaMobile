package com.spacca.app.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.LightGrey

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Privacy Policy", onBack = onBack)

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            DefaultText(
                text = "1. Information We Collect",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            DefaultText(
                text = "We may collect personal information such as your name, email address, phone number, location data, and order details when you use the Spacca application. This information is used solely to provide and improve our services.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 20,
                modifier = Modifier.padding(top = 6.dp)
            )

            DefaultText(
                text = "2. How We Use Your Information",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            DefaultText(
                text = "Your information is used to process orders, personalize your experience, improve our services, and communicate with you about promotions, updates, and other relevant information. We never sell your personal data to third parties.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 20,
                modifier = Modifier.padding(top = 6.dp)
            )

            DefaultText(
                text = "3. Data Sharing",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            DefaultText(
                text = "We may share aggregated, anonymized data with our partners and service providers to help us operate and improve the platform. We may also disclose your information if required by law or in connection with a merger, acquisition, or sale of assets.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 20,
                modifier = Modifier.padding(top = 6.dp)
            )

            DefaultText(
                text = "4. Data Security",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            DefaultText(
                text = "We implement industry-standard security measures to protect your personal information against unauthorized access, alteration, disclosure, or destruction. However, please note that no method of transmission over the internet is completely secure.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 20,
                modifier = Modifier.padding(top = 6.dp)
            )

            DefaultText(
                text = "5. Your Rights",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            DefaultText(
                text = "You have the right to access, correct, and request deletion of your personal data at any time. You may also opt out of marketing communications by following the unsubscribe link in any email or adjusting your account settings.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 20,
                modifier = Modifier.padding(top = 6.dp)
            )

            DefaultText(
                text = "6. Cookies",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            DefaultText(
                text = "Our application may use cookies and similar tracking technologies to enhance your experience, analyze usage patterns, and serve personalized content. You can manage your cookie preferences through your device settings.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 20,
                modifier = Modifier.padding(top = 6.dp)
            )

            DefaultText(
                text = "7. Changes to This Policy",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            DefaultText(
                text = "We may update this Privacy Policy from time to time to reflect changes in our practices or regulatory requirements. The effective date of the current policy is displayed at the top of this page. Continued use of the service constitutes acceptance of the updated policy.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 20,
                modifier = Modifier.padding(top = 6.dp)
            )

            DefaultText(
                text = "8. Contact Us",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            DefaultText(
                text = "If you have any questions or concerns about this Privacy Policy, please contact us at privacy@spacca.com or through the help section within the application. We are committed to addressing any privacy-related inquiries promptly.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 20,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
