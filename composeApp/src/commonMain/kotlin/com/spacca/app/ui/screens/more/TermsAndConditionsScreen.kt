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
fun TermsAndConditionsScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Terms and Conditions", onBack = onBack)

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            DefaultText(
                text = "1. Acceptance of Terms",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            DefaultText(
                text = "By accessing and using the Spacca application, you accept and agree to be bound by the terms and provisions outlined herein. If you do not agree to these terms, please discontinue use of the service immediately.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 20,
                modifier = Modifier.padding(top = 6.dp)
            )

            DefaultText(
                text = "2. Use of the Service",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            DefaultText(
                text = "The service is provided for your personal, non-commercial use only. You agree not to use the platform for any unlawful purposes or in any way that violates applicable laws, regulations, or third-party rights.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 20,
                modifier = Modifier.padding(top = 6.dp)
            )

            DefaultText(
                text = "3. Accounts and Security",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            DefaultText(
                text = "You are responsible for maintaining the confidentiality of your account credentials and for all activities that occur under your account. You must notify us immediately upon becoming aware of any unauthorized access or breach of security.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 20,
                modifier = Modifier.padding(top = 6.dp)
            )

            DefaultText(
                text = "4. Orders and Payments",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            DefaultText(
                text = "All orders are subject to availability and confirmation. Prices displayed on the platform may be subject to change without prior notice. Payment is processed securely through our partnered payment providers.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 20,
                modifier = Modifier.padding(top = 6.dp)
            )

            DefaultText(
                text = "5. Intellectual Property",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            DefaultText(
                text = "All content, including but not limited to text, graphics, logos, and software, is the exclusive property of Spacca and is protected by applicable intellectual property laws. You may not reproduce, distribute, or create derivative works without prior written consent.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 20,
                modifier = Modifier.padding(top = 6.dp)
            )

            DefaultText(
                text = "6. Limitation of Liability",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            DefaultText(
                text = "Spacca shall not be liable for any indirect, incidental, special, or consequential damages arising out of or in any way connected with the use of the service. Your sole remedy shall be limited to discontinuation of use.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 20,
                modifier = Modifier.padding(top = 6.dp)
            )

            DefaultText(
                text = "7. Changes to These Terms",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            DefaultText(
                text = "We reserve the right to modify or replace these terms at any time. Continued use of the service after any changes constitutes acceptance of the revised terms. The updated terms will be posted on this page with the date of the last revision.",
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
                text = "If you have any questions about these Terms and Conditions, please contact us at support@spacca.com or through the help section within the application. Our team is available to assist you during business hours.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 20,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
