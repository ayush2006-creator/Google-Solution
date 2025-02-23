package com.example.recovery_partner

 sealed class Screens (val route :String){
     object Quest1:Screens("question_1")
     object Quest2:Screens("question_2")
     object Quest3:Screens("question_3")

}