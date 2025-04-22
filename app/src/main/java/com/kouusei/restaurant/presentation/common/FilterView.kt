package com.kouusei.restaurant.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


enum class DistanceRange(val value: Int, val description: String) {
    RANGE_300M(1, "300m"),
    RANGE_500M(2, "500m"),
    RANGE_1000M(3, "1000m"),
    RANGE_2000M(4, "2000m"),
    RANGE_3000M(5, "3000m"),
    RANGE_NO(0, "現在地");

    companion object {
        fun fromValue(value: Int): DistanceRange? {
            return DistanceRange.entries.find { it.value == value }
        }
    }
}

enum class Filter(val value: String, val description: String) {
    Filter_FreeDrink("free_drink", "飲み放題"),
    Filter_FreeFood("free_food", "食べ放題"),
    Filter_PrivateRoom("private_room", "個室あり");

}

@Composable
fun FilterView(
    filterList: List<Filter> = Filter.entries,
    onDistanceChange: (DistanceRange) -> Unit,
    onFilterChange: (List<Filter>) -> Unit,
    selectedDistance: DistanceRange
) {
    val selectedFilters = remember { mutableStateListOf<Filter>() }

    // 控制距离下拉菜单
    var distanceMenuExpanded by remember { mutableStateOf(false) }
    val distanceOptions = DistanceRange.entries
//    var selectedDistance by remember { mutableStateOf<DistanceRange>(DistanceRange.RANGE_1000M) }

    LazyRow(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp)
            .fillMaxWidth()
    ) {
        item {
            Box {
                FilterChip(
                    label = selectedDistance.description,
                    isSelected = selectedDistance != DistanceRange.RANGE_NO,
                    onClick = { distanceMenuExpanded = true }
                )
                DropdownMenu(
                    expanded = distanceMenuExpanded,
                    onDismissRequest = { distanceMenuExpanded = false }
                ) {
                    distanceOptions.forEach { option ->
                        if (option == DistanceRange.RANGE_NO) {
                            DropdownMenuItem(
                                text = { Text("クリア") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear"
                                    )
                                },
                                onClick = {
//                                    selectedDistance = option
                                    onDistanceChange(option)
                                    distanceMenuExpanded = false
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text(option.description) },
                                onClick = {
//                                    selectedDistance = option
                                    onDistanceChange(option)
                                    distanceMenuExpanded = false
                                }
                            )
                        }

                    }
                }
            }
        }
        items(filterList) { item ->
            FilterChip(
                label = item.description,
                isSelected = selectedFilters.contains(item),
                onClick = {
                    if (selectedFilters.contains(item)) {
                        selectedFilters.remove(item)
                    } else {
                        selectedFilters.add(item)
                    }
                }
            )
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}