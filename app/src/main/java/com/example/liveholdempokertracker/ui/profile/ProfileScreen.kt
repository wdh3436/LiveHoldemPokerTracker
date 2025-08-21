package com.example.liveholdempokertracker.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.liveholdempokertracker.data.Tag

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileWithTags by viewModel.selectedProfileWithTags.collectAsState()

    profileWithTags?.let { data ->
        var memoText by remember(data.profile.memo) { mutableStateOf(data.profile.memo) }
        var showTagDialog by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(data.profile.name, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Tags
            TagSection(tags = data.tags, onAddClick = { showTagDialog = true }, onRemoveClick = { viewModel.removeTagFromCurrentProfile(it) })

            Spacer(modifier = Modifier.height(24.dp))
            StatsCard(profile = data.profile)
            Spacer(modifier = Modifier.height(24.dp))

            // Memo
            Text("메모", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = memoText,
                onValueChange = { memoText = it },
                modifier = Modifier.fillMaxWidth().weight(1f),
                label = { Text("플레이어에 대한 정보를 입력하세요") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.updateMemo(memoText) },
                modifier = Modifier.align(Alignment.End),
                enabled = memoText != data.profile.memo
            ) {
                Text("메모 저장")
            }
        }

        if (showTagDialog) {
            AddTagDialog(
                viewModel = viewModel,
                onDismiss = { showTagDialog = false }
            )
        }
    }
}

@Composable
fun TagSection(tags: List<Tag>, onAddClick: () -> Unit, onRemoveClick: (Tag) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("태그", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onAddClick, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add Tag")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tags) { tag ->
                TagChip(tag = tag, onRemoveClick = { onRemoveClick(tag) })
            }
        }
    }
}

@Composable
fun TagChip(tag: Tag, onRemoveClick: () -> Unit, showRemoveIcon: Boolean = true) {
    Card(shape = MaterialTheme.shapes.medium) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(16.dp).background(Color(tag.tagColor.toULong()), CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(tag.tagName, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(4.dp))
            if (showRemoveIcon) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Tag",
                    modifier = Modifier.size(16.dp).clickable { onRemoveClick() }
                )
            }
        }
    }
}

@Composable
fun AddTagDialog(viewModel: ProfileViewModel, onDismiss: () -> Unit) {
    val allTags by viewModel.allTags.collectAsState()
    var newTagName by remember { mutableStateOf("") }
    // Basic color selection
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta)
    var selectedColor by remember { mutableStateOf(colors.first()) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("태그 추가", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                // Create new tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = newTagName, onValueChange = { newTagName = it }, label = { Text("새 태그 이름") }, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        viewModel.createNewTag(newTagName, selectedColor)
                        newTagName = ""
                    }, enabled = newTagName.isNotBlank()) {
                        Text("생성")
                    }
                }
                Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { color ->
                        Box(modifier = Modifier.size(24.dp).background(color, CircleShape).clickable {
                            selectedColor = color
                        })
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                // Existing tags
                Text("기존 태그 선택", style = MaterialTheme.typography.titleMedium)
                LazyRow(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(allTags) { tag ->
                        // Wrap TagChip and Delete button in a Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.addTagToCurrentProfile(tag) } // Clickable for adding
                        ) {
                            TagChip(tag = tag, onRemoveClick = { /* In dialog, removal is not the primary action */ }, showRemoveIcon = false)
                            // Add a delete button for the tag definition itself
                            IconButton(
                                onClick = { viewModel.deleteTag(tag) }, // Call deleteTag from ViewModel
                                modifier = Modifier.size(24.dp) // Adjust size as needed
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Delete Tag")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("닫기")
                }
            }
        }
    }
}


@Composable
fun StatsCard(profile: com.example.liveholdempokertracker.data.PlayerProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatRow("VPIP", "${profile.getVpip()}%")
            StatRow("PFR", "${profile.getPfr()}%")
            StatRow("총 핸드 수", "${profile.handsPlayed}")
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.headlineSmall)
    }
}