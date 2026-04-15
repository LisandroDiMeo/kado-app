package com.kado.app.domain.model

data class Tutorial(val title: String, val steps: List<TutorialStep>)

data class TutorialStep(val title: String, val description: String)
