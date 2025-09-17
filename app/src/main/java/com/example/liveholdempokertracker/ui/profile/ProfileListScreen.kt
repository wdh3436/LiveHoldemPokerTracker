package com.example.liveholdempokertracker.ui.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.liveholdempokertracker.data.PlayerProfile
import com.example.liveholdempokertracker.data.ProfileWithTags
import com.example.liveholdempokertracker.ui.navigation.Screen
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.liveholdempokertracker.data.Tag

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileListScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    val allProfiles by viewModel.profiles.collectAsState()
    val recentProfiles by viewModel.recentProfiles.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    var newProfileName by remember { mutableStateOf("") }

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf("전체", "최근")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("프로필 관리", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = viewModel::onSearchTextChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("검색") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = pagerState.currentPage,
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val listToShow = if (page == 0) allProfiles else recentProfiles
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(listToShow) { profileWithTags ->
                    ProfileListItem(
                        profileWithTags = profileWithTags,
                        onDelete = { profile -> viewModel.deleteProfile(profile) },
                        onItemClick = { profileId -> navController.navigate(Screen.Profile.createRoute(profileId)) }
                    )
                    Divider()
                }
            }
        }

        if (pagerState.currentPage == 0) {
            Spacer(modifier = Modifier.height(16.dp))

            // Add new profile UI
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newProfileName,
                    onValueChange = { newProfileName = it },
                    label = { Text("새 프로필 이름") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        viewModel.addProfile(newProfileName)
                        newProfileName = "" // Clear input field
                    },
                    enabled = newProfileName.isNotBlank()
                ) {
                    Text("추가")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate(Screen.MergeProfile.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("프로필 데이터 합치기")
            }
        }
    }
}

@Composable
fun ProfileListItem(
    profileWithTags: ProfileWithTags,
    onDelete: (PlayerProfile) -> Unit,
    onItemClick: (Int) -> Unit
) {
    val profile = profileWithTags.profile
    val tags = profileWithTags.tags

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(profile.id) }
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = profile.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "VPIP: ${profile.getVpip()}%", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "PFR: ${profile.getPfr()}%", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { onDelete(profile) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Profile")
            }
        }
        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(tags) { tag ->
                    TagChip(tag = tag, onRemoveClick = { /* No removal from list item */ })
                }
            }
        }
    }
}

@Composable
fun TagChip(tag: Tag, onRemoveClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.padding(2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(tag.tagColor))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tag.tagName,
                color = if (Color(tag.tagColor).luminance() > 0.5) Color.Black else Color.White,
                style = MaterialTheme.typography.labelSmall
            )
            if (onRemoveClick != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Tag",
                    modifier = Modifier
                        .size(12.dp)
                        .clickable { onRemoveClick() },
                    tint = if (Color(tag.tagColor).luminance() > 0.5) Color.Black else Color.White
                )
            }
        }
    }
}