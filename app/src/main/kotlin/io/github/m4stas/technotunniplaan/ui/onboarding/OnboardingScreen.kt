package io.github.m4stas.technotunniplaan.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.Entity
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette

@Composable fun OnboardingScreen(state: OnboardingState, onRole: (Role) -> Unit, onBack: () -> Unit, onQuery: (String) -> Unit, onPick: (Entity) -> Unit, onRetry: () -> Unit) {
    val p = LocalPalette.current
    BackHandler(state is OnboardingState.Pick, onBack)
    Surface(Modifier.fillMaxSize(), color = p.background) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            when (state) {
                OnboardingState.Loading -> CircularProgressIndicator(color = p.accentText)
                OnboardingState.Offline -> {
                    Text(stringResource(R.string.offline_title), style = MaterialTheme.typography.headlineMedium, color = p.text)
                    Text(stringResource(R.string.offline_body), color = p.textSecondary)
                    Button(onRetry) { Text(stringResource(R.string.retry)) }
                }
                OnboardingState.ChooseRole -> {
                    Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.headlineMedium, color = p.text)
                    Role.entries.forEach { role ->
                        Card(onClick = { onRole(role) }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = p.card)) {
                            Text(stringResource(if (role == Role.STUDENT) R.string.role_student else R.string.role_teacher), Modifier.fillMaxWidth().padding(24.dp), color = p.text)
                        }
                    }
                }
                is OnboardingState.Pick -> {
                    Text(stringResource(if (state.role == Role.STUDENT) R.string.pick_class else R.string.pick_teacher), style = MaterialTheme.typography.headlineMedium, color = p.text)
                    OutlinedTextField(state.query, onQuery, label = { Text(stringResource(R.string.search)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(24.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.results, key = { it.key }) { entity ->
                            Card(onClick = { onPick(entity) }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = p.card)) {
                                Text(entity.name, Modifier.fillMaxWidth().padding(20.dp), color = p.text)
                            }
                        }
                    }
                }
            }
        }
    }
}
