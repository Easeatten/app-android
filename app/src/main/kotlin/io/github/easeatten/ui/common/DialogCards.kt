package io.github.easeatten.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun <T> RadioButtonDialogCard(
    modifier: Modifier = Modifier,
    title: String,
    options: Iterable<T>,
    optionsToLabels: (T) -> String,
    selected: T,
    onSelectedChange: (T) -> Unit,
) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Column {
            Text(
                modifier = Modifier.padding(20.dp),
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )

            LazyColumn(modifier = Modifier.selectableGroup()) {
                items(options.toList()) { elem ->
                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                                .selectable(
                                    role = Role.RadioButton,
                                    selected = (selected == elem),
                                    onClick = { onSelectedChange(elem) },
                                )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                selected = (selected == elem),
                                onClick = null,
                            )

                            Text(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                text = optionsToLabels(elem),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 10.dp))
        }
    }
}

@Composable
fun ConfirmDialogCard(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    positiveButtonText: String,
    negativeButtonText: String,
    onPositiveButtonClick: () -> Unit,
    onNegativeButtonClick: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(text = message)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onPositiveButtonClick) { Text(text = positiveButtonText) }
                Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                TextButton(onClick = onNegativeButtonClick) { Text(text = negativeButtonText) }
            }
        }
    }
}
