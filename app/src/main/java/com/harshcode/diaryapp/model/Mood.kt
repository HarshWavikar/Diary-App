package com.harshcode.diaryapp.model

import androidx.compose.ui.graphics.Color
import com.harshcode.diaryapp.R
import com.harshcode.diaryapp.ui.theme.AngryColor
import com.harshcode.diaryapp.ui.theme.AwfulColor
import com.harshcode.diaryapp.ui.theme.BoredColor
import com.harshcode.diaryapp.ui.theme.CalmColor
import com.harshcode.diaryapp.ui.theme.DepressedColor
import com.harshcode.diaryapp.ui.theme.DisappointedColor
import com.harshcode.diaryapp.ui.theme.HappyColor
import com.harshcode.diaryapp.ui.theme.HumorousColor
import com.harshcode.diaryapp.ui.theme.LonelyColor
import com.harshcode.diaryapp.ui.theme.NeutralColor
import com.harshcode.diaryapp.ui.theme.RomanticColor
import com.harshcode.diaryapp.ui.theme.SurprisedColor
import com.harshcode.diaryapp.ui.theme.SuspiciousColor
import com.harshcode.diaryapp.ui.theme.WinkColor

enum class Mood(
    val icon: Int,
    val contentColor: Color,
    val containerColor: Color,
) {
    Neutral(
        icon = R.drawable.neutral,
        contentColor = Color.White,
        containerColor = NeutralColor
    ),
    Happy(
    icon = R.drawable.happy,
    contentColor = Color.Black,
    containerColor = HappyColor
    ),
    Angry(
        icon = R.drawable.angry,
        contentColor = Color.White,
        containerColor = AngryColor
    ),
    Bored(
        icon = R.drawable.bored,
        contentColor = Color.Black,
        containerColor = BoredColor
    ),
    Calm(
        icon = R.drawable.calm,
        contentColor = Color.Black,
        containerColor = CalmColor
    ),
    Depressed(
        icon = R.drawable.depressed,
        contentColor = Color.Black,
        containerColor = DepressedColor
    ),
    Disappointed(
        icon = R.drawable.depressed,
        contentColor = Color.White,
        containerColor = DisappointedColor
    ),
    Humorous(
        icon = R.drawable.humorous,
        contentColor = Color.Black,
        containerColor = HumorousColor
    ),
    Lonely(
        icon = R.drawable.lonely,
        contentColor = Color.Black,
        containerColor = LonelyColor
    ),
    Wink(
    icon = R.drawable.wink,
    contentColor = Color.White,
    containerColor = WinkColor
    ),
    Surprised(
        icon = R.drawable.surprised,
        contentColor = Color.Black,
        containerColor = SurprisedColor
    ),
    Suspicious(
        icon = R.drawable.suspicious,
        contentColor = Color.Black,
        containerColor = SuspiciousColor
    ),
    Romantic(
        icon = R.drawable.love_kiss,
        contentColor = Color.Black,
        containerColor = RomanticColor
    ),
    Awful(
        icon = R.drawable.awful,
        contentColor = Color.Black,
        containerColor = AwfulColor
    )
}