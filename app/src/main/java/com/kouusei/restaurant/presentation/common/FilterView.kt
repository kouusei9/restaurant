package com.kouusei.restaurant.presentation.common

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kouusei.restaurant.R
import com.kouusei.restaurant.presentation.entities.SearchFilters

val TAG = "FilterView"

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
    Order_Recommend(4, "おすすめ順"),
    Order_Distance(0, "距離順");

    companion object {
        fun fromDes(des: String): OrderMethod? {
            return OrderMethod.entries.find { it.description == des }
        }
    }
}

enum class Genre(val value: String, val description: String) {
    Genre_G001("G001", "居酒屋"),
    Genre_G002("G002", "ダイニングバー・バル"),
    Genre_G003("G003", "創作料理"),
    Genre_G004("G004", "和食"),
    Genre_G005("G005", "洋食"),
    Genre_G006("G006", "イタリアン・フレンチ"),
    Genre_G007("G007", "中華"),
    Genre_G008("G008", "焼肉・ホルモン"),
    Genre_G009("G009", "アジア・エスニック料理"),
    Genre_G010("G010", "各国料理"),
    Genre_G011("G011", "カラオケ・パーティ"),
    Genre_G012("G002", "バー・カクテル"),
    Genre_G013("G013", "ラーメン"),
    Genre_G014("G014", "カフェ・スイーツ"),
    Genre_G015("G015", "その他グルメ"),
    Genre_G016("G016", "お好み焼き・もんじゃ"),
    Genre_G017("G017", "韓国料理"),
    Genre_G000("G000", "クリア  ✖️");

    companion object {
        fun fromDes(des: String): Genre {
            return Genre.entries.find { it.description == des } ?: Genre_G000
        }
    }
}

enum class Filter(val value: String, val description: String) {
    Filter_FreeDrink("free_drink", "飲み放題"),
    Filter_FreeFood("free_food", "食べ放題"),
    Filter_PrivateRoom("private_room", "個室あり"),
    Filter_Wifi("wifi", "Wifi"),
    Filter_Course("course", "コース"),
    Filter_Card("card", "カード"),
    Filter_Non_Smoking("non_smoking", "禁煙"),
    Filter_Parking("parking", "駐車場"),
    Filter_Lunch("lunch", "ランチ"),
    Filter_English("english", "English"),
    Filter_Pet("pet", "ペット")
    ;
}

@Composable
fun FilterView(
    filterList: List<Filter> = Filter.entries,
    onDistanceChange: (DistanceRange) -> Unit,
    selectedOrderMethod: OrderMethod,
    onOrderMethodChange: (OrderMethod) -> Unit,
    selectedGenre: Genre,
    onSelectedGenreChange: (Genre) -> Unit,
    onFilterChange: (Filter) -> Unit,
    selectedDistance: DistanceRange,
    state: SearchFilters,
    keyword: String,
    listState: LazyListState
) {
    LazyRow(
        state = listState,
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp)
            .fillMaxWidth()
            .height(40.dp)
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
                    label = "ソート",
                    isSelected = true,
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
        item {
            Box {
                FilterChipWithDropdownMenu(
                    label = stringResource(R.string.filter_genre),
                    isSelected = selectedGenre != Genre.Genre_G000,
                    options = Genre.entries.map { it.description },
                    selected = selectedGenre.description
                ) {
                    val genre = Genre.fromDes(it)
                    if (genre != selectedGenre) {
                        Log.d(TAG, "FilterView: description of genre $it")
                        onSelectedGenreChange(genre)
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
    label: String,
    isSelected: Boolean,
    selected: String,
    onSelectedChanged: (String) -> Unit,
) {
    var distanceMenuExpanded by remember { mutableStateOf(false) }
    FilterChip(
        label = if (isSelected) selected else label,
        isSelected = isSelected,
        isDropDown = true,
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
        isDropDown = true,
        onClick = { distanceMenuExpanded = true }
    )
    DropdownMenu(
        expanded = distanceMenuExpanded,
        onDismissRequest = { distanceMenuExpanded = false }
    ) {
        distanceOptions.forEach { option ->
            if (option == selectedDistance) {
                // show nothing.
            } else if (option == DistanceRange.RANGE_NO) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.filter_clear)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear"
                        )
                    },
                    onClick = {
                        onDistanceChange(DistanceRange.RANGE_NO)
                        distanceMenuExpanded = false
                    }
                )
            } else {
                DropdownMenuItem(
                    text = { Text(option.description) },
                    onClick = {
                        if (option != selectedDistance) {
                            onDistanceChange(option)
                        }
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
    isDropDown: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .height(30.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                start = 8.dp,
                end = if (isDropDown) 4.dp else 8.dp,
                top = 4.dp,
                bottom = 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall
            )

            if (isDropDown) {
                Icon(
                    Icons.Default.ArrowDropDown, contentDescription = "drop down"
                )
            }
        }
    }
}

@Preview
@Composable
fun FilterChipPreview() {
    FilterChip(
        label = "test",
        isSelected = false,
    ) { }
}