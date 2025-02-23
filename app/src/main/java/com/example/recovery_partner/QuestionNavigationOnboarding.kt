package com.example.recovery_partner
import android.health.connect.datatypes.ExerciseRoute
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
@Composable
fun Navigation(){
    val navController = rememberNavController()
    NavHost(navController=navController, startDestination =Screens.Quest1.route){
        composable(route = Screens.Quest1.route){
            QuestionLayout( navController=navController, question = "how are you",route =Screens.Quest2.route)
        }
        composable(route =Screens.Quest2.route ){
            QuestionLayout( navController=navController, question = "how are you2",route =Screens.Quest3.route)
        }
        composable(route =Screens.Quest3.route ){
            QuestionLayout( navController=navController, question = "how are you3",route =Screens.Quest1.route)
        }
    }
}

@Composable
fun QuestionLayout(navController: NavController,question:String,route:String){
    Column {
        Text(text=question, )
        CircleRow(navController= navController,route=route)

        }
    }


@Composable
fun CircleRow(navController: NavController,route:String) {
    val defaultColors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow)
    val clickedColors = listOf(Color.Magenta, Color.Cyan, Color.Gray, Color.Black)
    var selectedIndex by remember { mutableStateOf(-1) }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        defaultColors.forEachIndexed { index, color ->
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        if (index == selectedIndex) clickedColors[index] else color,
                        shape = CircleShape
                    )
                    .clickable {
                        selectedIndex = if (selectedIndex == index) -1 else index
                    }
            )
        }
    }
    Button(onClick = {if (selectedIndex != -1) navController.navigate(route)  }) { Text(text = "Submit")}
}

