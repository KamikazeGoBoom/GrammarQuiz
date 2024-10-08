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
                    },
                    getAnalyticsData = { getAnalyticsData() }
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

    private fun getAnalyticsData(): Map<String, Any> {
        val categories = listOf("VERB", "NOUN", "ADJECTIVE", "ADVERB", "SENTENCE")
        val sharedPref = getSharedPreferences("QuizPrefs", MODE_PRIVATE)

        val categoryScores = categories.associateWith { category ->
            (1..5).mapNotNull { index ->
                sharedPref.getInt("${category}_SCORE_$index", -1).takeIf { it != -1 }
            }
        }

        val overallAverage = categoryScores.values.flatten().takeIf { it.isNotEmpty() }?.average() ?: 0.0
        val categoryAverages = categoryScores.mapValues { (_, scores) ->
            scores.takeIf { it.isNotEmpty() }?.average() ?: 0.0
        }
        val mostImprovedCategory = categoryScores.entries.maxByOrNull { (_, scores) ->
            if (scores.size >= 2) scores.last() - scores.first() else 0
        }?.key ?: "N/A"
        val totalQuizzesTaken = categoryScores.values.sumOf { it.size }

        return mapOf(
            "overallAverage" to overallAverage,
            "categoryAverages" to categoryAverages,
            "mostImprovedCategory" to mostImprovedCategory,
            "totalQuizzesTaken" to totalQuizzesTaken
        )
    }
}

@Composable
fun MainScreen(
    onStartQuiz: (String) -> Unit,
    getProgressData: () -> Map<String, Int>,
    getAnalyticsData: () -> Map<String, Any>
) {
    var progressData by remember { mutableStateOf(getProgressData()) }
    var analyticsData by remember { mutableStateOf(getAnalyticsData()) }

    LaunchedEffect(Unit) {
        progressData = getProgressData()
        analyticsData = getAnalyticsData()
    }

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

        QuizButton("Verb Quiz", "VERB") {
            onStartQuiz("VERB")
            progressData = getProgressData()
            analyticsData = getAnalyticsData()
        }
        QuizButton("Noun Quiz", "NOUN") {
            onStartQuiz("NOUN")
            progressData = getProgressData()
            analyticsData = getAnalyticsData()
        }
        QuizButton("Adjective Quiz", "ADJECTIVE") {
            onStartQuiz("ADJECTIVE")
            progressData = getProgressData()
            analyticsData = getAnalyticsData()
        }
        QuizButton("Adverb Quiz", "ADVERB") {
            onStartQuiz("ADVERB")
            progressData = getProgressData()
            analyticsData = getAnalyticsData()
        }
        QuizButton("Sentence Structure Quiz", "SENTENCE") {
            onStartQuiz("SENTENCE")
            progressData = getProgressData()
            analyticsData = getAnalyticsData()
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Analytics Dashboard",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Overall Average Score: ${String.format("%.2f", analyticsData["overallAverage"] as Double)}",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Total Quizzes Taken: ${analyticsData["totalQuizzesTaken"]}",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Most Improved Category: ${analyticsData["mostImprovedCategory"]}",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Category Averages:",
            style = MaterialTheme.typography.bodyLarge
        )

        (analyticsData["categoryAverages"] as Map<String, Double>).forEach { (category, average) ->
            Text(
                text = "$category: ${String.format("%.2f", average)}",
                style = MaterialTheme.typography.bodyMedium
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
fun QuizButton(text: String, category: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
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