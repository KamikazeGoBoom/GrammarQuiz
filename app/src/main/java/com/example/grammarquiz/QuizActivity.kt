package com.example.grammarquiz

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

class QuizActivity : ComponentActivity() {
    private val verbQuestions = listOf(
        Question("Which of the following is a regular verb?", listOf("Go", "Be", "Have", "Walk", "Eat"), 3),
        Question("What is the past participle of 'write'?", listOf("Wrote", "Written", "Writed", "Writing", "Wrought"), 1),
        Question("Which verb tense is used in: 'I have been studying for hours'?", listOf("Simple Present", "Present Perfect", "Present Continuous", "Present Perfect Continuous", "Past Perfect"), 3),
        Question("Identify the correct form: 'If I ___ rich, I would travel the world.'", listOf("am", "was", "were", "be", "is"), 2),
        Question("Which sentence uses the subjunctive mood correctly?", listOf(
            "I wish I was taller.",
            "If I was you, I wouldn't do that.",
            "I suggest that he goes to the doctor.",
            "It is essential that she be on time.",
            "If only he was here to help us."
        ), 3)
    )

    private val nounQuestions = listOf(
        Question("Which of these is an abstract noun?", listOf("Table", "Dog", "Happiness", "Book", "Tree"), 2),
        Question("What is the plural of 'criterion'?", listOf("Criterions", "Criteria", "Criterias", "Criterions", "Criterion"), 1),
        Question("Identify the collective noun: 'A ___ of wolves'", listOf("Pack", "Herd", "Flock", "Swarm", "School"), 0),
        Question("Which noun is uncountable?", listOf("Chair", "Idea", "Water", "Mountain", "Child"), 2),
        Question("In the phrase 'The car's engine', what type of noun is 'car's'?", listOf("Common Noun", "Proper Noun", "Possessive Noun", "Compound Noun", "Abstract Noun"), 2)
    )

    private val adjectiveQuestions = listOf(
        Question("Which word is a comparative adjective?", listOf("Good", "Better", "Best", "Well", "Goodest"), 1),
        Question("In 'The old, rusty car', which is NOT an adjective?", listOf("The", "Old", "Rusty", "Car", "Old and Rusty"), 3),
        Question("What is the correct order for multiple adjectives: 'A ___ Italian racing car'", listOf(
            "red fast old",
            "old red fast",
            "fast old red",
            "old fast red",
            "fast red old"
        ), 1),
        Question("Which sentence uses an adjective in the predicate position?", listOf(
            "The happy child played in the park.",
            "She bought a new car.",
            "The movie seems interesting.",
            "That tall building is an office.",
            "He wears a golden watch."
        ), 2),
        Question("What type of adjective is 'their' in 'Their house is beautiful'?", listOf("Demonstrative", "Possessive", "Interrogative", "Indefinite", "Numeric"), 1)
    )

    private val adverbQuestions = listOf(
        Question("Which word is an adverb of manner?", listOf("Soon", "Here", "Quickly", "Very", "Almost"), 2),
        Question("In 'She sang beautifully', what does 'beautifully' modify?", listOf("She", "Sang", "Both She and Sang", "Neither", "The entire sentence"), 1),
        Question("Which sentence uses an adverb of frequency?", listOf(
            "He runs fast.",
            "They arrived yesterday.",
            "She often visits her grandmother.",
            "The cat is sleeping there.",
            "He spoke loudly."
        ), 2),
        Question("What is the comparative form of the adverb 'well'?", listOf("Weller", "More well", "Better", "Best", "Gooder"), 2),
        Question("Identify the adverb in: 'The very tall man walked slowly.'", listOf("Very", "Tall", "Man", "Walked", "Slowly"), 4)
    )

    private val sentenceQuestions = listOf(
        Question("Which sentence is in the passive voice?", listOf(
            "The cat chased the mouse.",
            "The mouse was chased by the cat.",
            "Chasing the mouse, the cat ran quickly.",
            "The mouse ran from the chasing cat.",
            "Cats usually chase mice."
        ), 1),
        Question("Identify the dependent clause: 'While I was sleeping, the phone rang.'", listOf(
            "While I was sleeping",
            "the phone rang",
            "While I",
            "was sleeping",
            "There is no dependent clause"
        ), 0),
        Question("Which sentence contains a dangling modifier?", listOf(
            "Running quickly, John caught the bus.",
            "The bus was caught by John, who was running quickly.",
            "Running quickly, the bus was caught by John.",
            "John caught the bus while running quickly.",
            "Quickly running, John caught the bus."
        ), 2),
        Question("What type of sentence is this: 'Stop!'", listOf("Declarative", "Interrogative", "Exclamatory", "Imperative", "Complex"), 3),
        Question("In the sentence 'The old man, who lived next door, was a retired teacher', what is the function of the phrase 'who lived next door'?", listOf(
            "Main Clause",
            "Subordinate Clause",
            "Relative Clause",
            "Adverbial Phrase",
            "Noun Phrase"
        ), 2)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val category = intent.getStringExtra("CATEGORY") ?: "VERB"
        val questions = when (category) {
            "VERB" -> verbQuestions
            "NOUN" -> nounQuestions
            "ADJECTIVE" -> adjectiveQuestions
            "ADVERB" -> adverbQuestions
            "SENTENCE" -> sentenceQuestions
            else -> verbQuestions
        }

        setContent {
            MaterialTheme {
                QuizScreen(
                    questions = questions,
                    category = category,
                    onQuizFinished = { score ->
                        saveQuizScore(category, score)
                        finish()
                    }
                )
            }
        }
    }

    private fun saveQuizScore(category: String, score: Int) {
        val sharedPref = getSharedPreferences("QuizPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Shift existing scores
        for (i in 4 downTo 1) {
            val prevScore = sharedPref.getInt("${category}_SCORE_$i", -1)
            if (prevScore != -1) {
                editor.putInt("${category}_SCORE_${i + 1}", prevScore)
            }
        }

        // Save new score
        editor.putInt("${category}_SCORE_1", score)
        editor.apply()
    }
}

@Composable
fun QuizScreen(questions: List<Question>, category: String, onQuizFinished: (Int) -> Unit) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentQuestionIndex < questions.size) {
            Text(
                text = "$category Quiz - Question ${currentQuestionIndex + 1}/${questions.size}",
                style = MaterialTheme.typography.headlineSmall
            )

            val currentQuestion = questions[currentQuestionIndex]
            Text(
                text = currentQuestion.text,
                style = MaterialTheme.typography.bodyLarge
            )

            currentQuestion.options.forEachIndexed { index, option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedAnswer == index,
                        onClick = { selectedAnswer = index }
                    )
                    Text(text = option, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Button(
                onClick = {
                    if (selectedAnswer != null) {
                        if (selectedAnswer == currentQuestion.correctAnswer) {
                            score++
                        }
                        currentQuestionIndex++
                        selectedAnswer = null
                    }
                },
                enabled = selectedAnswer != null
            ) {
                Text("Submit")
            }
        } else {
            Text(
                text = "Quiz Finished!",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Your score is $score out of ${questions.size}",
                style = MaterialTheme.typography.headlineSmall
            )
            Button(onClick = { onQuizFinished(score) }) {
                Text("Finish")
            }
        }
    }
}

data class Question(val text: String, val options: List<String>, val correctAnswer: Int)