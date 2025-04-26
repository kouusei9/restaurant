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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kouusei.restaurant.presentation.entities.SearchFilters


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

enum class OrderMethod(val value: Int, val description: String) {
    Order_Recommend(4, "おすすめ"),
    Order_Distance(0, "距離");

    companion object {
        fun fromDes(des: String): OrderMethod? {
            return OrderMethod.entries.find { it.description == des }
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
    selectedOrderMethod: OrderMethod,
    onOrderMethodChange: (OrderMethod) -> Unit,
    onFilterChange: (Filter) -> Unit,
    selectedDistance: DistanceRange,
    state: SearchFilters,
    keyword: String
) {
    LazyRow(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp)
            .fillMaxWidth()
    ) {
        item {
            Box {
                DistanceFilterChip(
                    selectedDistance = selectedDistance,
                    keyword = keyword,
                    onDistanceChange = onDistanceChange
                )
            }
        }
        item {
            Box {
                FilterChipWithDropdownMenu(
                    options = OrderMethod.entries.map { it.description },
                    selected = selectedOrderMethod.description
                ) {
                    val method = OrderMethod.fromDes(it)
                    method?.let {
                        if (method != selectedOrderMethod) {
                            onOrderMethodChange(method)
                        }
                    }
                }
            }
        }
        items(filterList) { item ->
            FilterChip(
                label = item.description,
                isSelected = state.getValue(item),
                onClick = { onFilterChange(item) }
            )
        }
    }
}

@Composable
fun FilterChipWithDropdownMenu(
    options: List<String>,
    selected: String,
    onSelectedChanged: (String) -> Unit,
) {
    var distanceMenuExpanded by remember { mutableStateOf(false) }
    FilterChip(
        label = selected,
        isSelected = true,
        onClick = { distanceMenuExpanded = true }
    )
    DropdownMenu(
        expanded = distanceMenuExpanded,
        onDismissRequest = { distanceMenuExpanded = false }
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option) },
                onClick = {
                    onSelectedChanged(option)
                    distanceMenuExpanded = false
                }
            )
        }
    }
}

@Composable
fun DistanceFilterChip(
    selectedDistance: DistanceRange,
    keyword: String,
    onDistanceChange: (DistanceRange) -> Unit,
) {
    var distanceMenuExpanded by remember { mutableStateOf(false) }
    val distanceOptions = DistanceRange.entries
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
            if (option == DistanceRange.RANGE_NO && keyword.isEmpty()) {
                // show nothing
            } else if (option == DistanceRange.RANGE_NO) {
                DropdownMenuItem(
                    text = { Text("クリア") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear"
                        )
                    },
                    onClick = {
                        if (option != selectedDistance) {
                            onDistanceChange(option)
                        }
                        distanceMenuExpanded = false
                    }
                )
            } else {
                DropdownMenuItem(
                    text = { Text(option.description) },
                    onClick = {
                        onDistanceChange(option)
                        distanceMenuExpanded = false
                    }
                )
            }
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