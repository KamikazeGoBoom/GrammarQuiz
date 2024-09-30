package com.example.grammarquiz

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen(
                    onStartQuiz = { category ->
                        val intent = Intent(this, QuizActivity::class.java)
                        intent.putExtra("CATEGORY", category)
                        startActivity(intent)
                    },
                    getProgressData = {
                        val categories = listOf("VERB", "NOUN", "ADJECTIVE", "ADVERB", "SENTENCE")
                        categories.associateWith { category ->
                            calculateCategoryScore(category)
                        }
                    }
                )
            }
        }
    }

    private fun calculateCategoryScore(category: String): Int {
        val sharedPref = getSharedPreferences("QuizPrefs", MODE_PRIVATE)
        val scores = (1..5).mapNotNull { index ->
            val score = sharedPref.getInt("${category}_SCORE_$index", -1)
            if (score != -1) score else null
        }
        return if (scores.isNotEmpty()) scores.average().roundToInt() else 0
    }
}

@Composable
fun MainScreen(onStartQuiz: (String) -> Unit, getProgressData: () -> Map<String, Int>) {
    var progressData by remember { mutableStateOf(getProgressData()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Grammar Quiz Categories",
            style = MaterialTheme.typography.headlineMedium
        )

        QuizButton("Verb Quiz", "VERB", onStartQuiz)
        QuizButton("Noun Quiz", "NOUN", onStartQuiz)
        QuizButton("Adjective Quiz", "ADJECTIVE", onStartQuiz)
        QuizButton("Adverb Quiz", "ADVERB", onStartQuiz)
        QuizButton("Sentence Structure Quiz", "SENTENCE", onStartQuiz)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Progress Report",
            style = MaterialTheme.typography.headlineSmall
        )

        progressData.forEach { (category, score) ->
            Text(
                text = "$category: $score",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Recommendations",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = generateRecommendations(progressData),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun QuizButton(text: String, category: String, onStartQuiz: (String) -> Unit) {
    Button(
        onClick = { onStartQuiz(category) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

fun generateRecommendations(progressData: Map<String, Int>): String {
    val recommendations = mutableListOf<String>()

    progressData.forEach { (category, score) ->
        when {
            score < 4 -> recommendations.add("Your $category skills need significant improvement. Focus on basic $category rules and practice regularly.")
            score < 7 -> recommendations.add("You have a good foundation in $category, but there's room for improvement. Try to identify and work on your weak areas in $category.")
            score < 9 -> recommendations.add("You're doing well in $category! To excel, focus on mastering complex $category rules and exceptions.")
            else -> recommendations.add("Excellent work in $category! To maintain your skills, challenge yourself with advanced $category exercises.")
        }
    }

    val overallScore = progressData.values.average()
    val overallRecommendation = when {
        overallScore < 4 -> "Your overall grammar skills need work. Start with the basics and gradually build up your knowledge."
        overallScore < 7 -> "You have a decent grasp of grammar, but there's significant room for improvement. Focus on your weaker areas."
        overallScore < 9 -> "Your grammar skills are good! To excel, pay attention to the nuances and exceptions in grammar rules."
        else -> "Your grammar skills are excellent! To further improve, explore advanced grammar concepts and practice writing complex sentences."
    }

    return overallRecommendation + "\n\n" + recommendations.joinToString("\n\n")
}