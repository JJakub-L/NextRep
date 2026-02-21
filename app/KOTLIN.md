# Przewodnik po języku Kotlin dla Projektu NextRep

Ten dokument pomoże Ci zrozumieć, jak czytać i pisać kod w Kotlinie, opierając się na przykładach z naszej aplikacji.

---

## 1. Zmienne: `val` vs `var`
W Kotlinie bardzo ważne jest rozróżnienie między danymi, które się zmieniają, a tymi, które są stałe.

- **`val` (Value):** Wartość "tylko do odczytu". Raz przypisana, nie może zostać zmieniona. Stosujemy ją wszędzie tam, gdzie to możliwe (bezpieczeństwo).
- **`var` (Variable):** Zmienna, którą można modyfikować.

```kotlin
val id = "123" // Nie zmieni się
var weight = "80" // Może zostać zmienione później
```

---

## 2. Data Class – Serce modeli danych
Większość Twoich modeli (jak `Exercise` czy `Workout`) to `data class`. Kotlin automatycznie generuje dla nich przydatne funkcje (jak `copy()`, `equals()`).

```kotlin
data class Exercise(val name: String, val reps: Int)

val ex1 = Exercise("Przysiad", 10)
val ex2 = ex1.copy(reps = 12) // Tworzy nowy obiekt ze zmienioną liczbą powtórzeń
```
*Dlaczego tak?* Ponieważ w programowaniu funkcyjnym wolimy tworzyć nowe wersje obiektów zamiast zmieniać stare (immutability). Zapobiega to błędom, w których dane zmieniają się "pod maską" w nieoczekiwanym momencie.

---

## 3. Parametry domyślne i nazwane
Kotlin pozwala na definiowanie domyślnych wartości w konstruktorach. Dzięki temu nie musisz podawać wszystkich danych przy tworzeniu obiektu.

```kotlin
data class ExerciseSet(
    val reps: Int,
    val weight: Double = 0.0, // Domyślnie 0.0
    val isCompleted: Boolean = false // Domyślnie false
)

// Możemy stworzyć obiekt podając tylko wymagane pole:
val set = ExerciseSet(reps = 10) 
// Dzięki nazwom parametrów kod jest bardzo czytelny:
val heavySet = ExerciseSet(reps = 5, weight = 100.0)
```

---

## 4. Bezpieczeństwo Null (Null Safety)
To jedna z najważniejszych cech Kotlina. Zapobiega najczęstszemu błędowi: `NullPointerException`.

- **`String`**: Nigdy nie może być nullem. Próba przypisania `null` spowoduje błąd już podczas pisania kodu.
- **`String?`**: Znak zapytania oznacza, że to pudełko może być puste (może przechowywać `null`).

**Operator Elvis (`?:`):** Służy do podawania wartości domyślnej, gdy coś jest nullem.
```kotlin
val result = weightInput.toDoubleOrNull() ?: 0.0 
// Jeśli tekst nie jest liczbą (wynik to null), użyj 0.0
```

---

## 5. Instrukcja `when` – Super Switch
`when` to potężniejszy odpowiednik `switch` z innych języków. Może być używany jako wyrażenie (zwraca wartość).

```kotlin
val color = when(day) {
    DayOfWeek.MONDAY -> Color.Red
    DayOfWeek.FRIDAY -> Color.Green
    else -> Color.Gray
}
```

---

## 6. String Templates (Szablony tekstu)
Zamiast łączyć tekst za pomocą `+`, w Kotlinie wstrzykujemy zmienne bezpośrednio do napisu używając symbolu `$`.

```kotlin
val name = "Adrian"
val weight = 90
println("Cześć $name, Twoja waga to $weight kg")
// Można też wykonywać operacje wewnątrz ${ }:
println("Za tydzień będziesz mieć ${weight + 1} kg")
```

---

## 7. Operacje na kolekcjach (Listy)
W `WorkoutViewModel` zobaczysz dużo operacji typu `map`, `filter`, `flatMap`. To "silnik" przetwarzania danych w Kotlinie.

- **`filter { ... }`**: Zostawia tylko elementy spełniające warunek.
- **`map { ... }`**: Zmienia każdy element listy na coś innego (np. z listy Workout na listę nazw).
- **`sumOf { ... }`**: Sumuje wartości liczbowe z obiektów.
- **`maxOfOrNull { ... }`**: Znajduje największą wartość (np. Twój rekord PR).

```kotlin
val completedWorkouts = allWorkouts.filter { it.isCompleted }
val totalScore = completedWorkouts.sumOf { it.totalScore }
```

---

## 8. Funkcje Zakresu (Scope Functions: `apply`, `let`, `also`)
Używamy ich, aby uprościć operacje na obiektach.

- **`apply`**: "Skonfiguruj ten obiekt i mi go oddaj". Bardzo częste przy ustawianiu kalendarza.
```kotlin
val cal = Calendar.getInstance().apply {
    timeInMillis = timestamp
    set(Calendar.HOUR_OF_DAY, 0)
}
```
- **`let`**: "Jeśli to nie jest null, zrób coś z tym".
```kotlin
summaryData?.let { data ->
    // Wykona się tylko, jeśli summaryData nie jest null
}
```

---

## 9. Delegacja Właściwości (`by`)
Słowo kluczowe `by` pozwala przekazać logikę zmiennej do innego obiektu. W Compose używamy tego do zarządzania stanem.

```kotlin
// Bez "by" (musisz pisać .value):
val state = remember { mutableStateOf("") }
state.value = "Nowy tekst"

// Z "by" (używasz jak zwykłej zmiennej):
var text by remember { mutableStateOf("") }
text = "Nowy tekst" // Automatycznie aktualizuje .value i odświeża UI
```

---

## 10. Extension Functions (Funkcje Rozszerzające)
Kotlin pozwala "dopisać" funkcje do istniejących klas bez modyfikowania ich kodu.

```kotlin
// Dodajemy funkcję do klasy String
fun String.toKg(): String = "$this kg"

val weight = "80"
println(weight.toKg()) // Wynik: "80 kg"
```

---

## 11. Enums i Sealed Classes
Służą do definiowania stałych zestawów wartości.

- **`enum class`**: Prosta lista opcji (np. dni tygodnia).
- **`sealed class`**: Bardziej zaawansowana struktura, gdzie każda opcja może mieć własne dane (używana często do stanów ekranu: Loading, Success, Error).

---

## 12. Listy: `List` vs `MutableList`
Kotlin wymusza rozróżnienie między listą, której nie można zmieniać, a taką, do której można dodawać elementy.

- **`List`**: Tylko do odczytu. Gwarantuje, że dane nie zmienią się niespodziewanie.
- **`MutableList`**: Pozwala na `add()`, `remove()`.

W `WorkoutViewModel` często robimy: `exercises.toList()`. Zmieniamy listę na "bezpieczną" wersję tylko do odczytu przed zapisaniem jej do bazy.

---

## 13. Coroutines i Flow (Asynchroniczność)
Używamy ich, żeby aplikacja nie "zacinała się" podczas zapisu do bazy danych.

- **`suspend fun`**: Funkcja, która może zostać wstrzymana (np. czekanie na bazę danych) bez blokowania ekranu.
- **`viewModelScope.launch { ... }`**: Uruchamia zadanie w tle.
- **`Flow`**: Strumień danych. Wyobraź sobie go jak rurę, przez którą płyną dane z bazy.
- **`StateFlow`**: Specjalny Flow, który zawsze przechowuje "aktualny stan" (np. listę Twoich planów). UI zawsze widzi najnowszą wersję.

---

## 14. Dlaczego kod w projekcie wygląda tak, a nie inaczej?

### Wzorce w ViewModelu:
```kotlin
fun toggleCompletion(workout: Workout) {
    val updatedWorkout = workout.copy(isCompleted = true)
    viewModelScope.launch {
        dao.insertWorkout(updatedWorkout)
    }
}
```
1. **`.copy(...)`**: Zamiast zmieniać pole w istniejącym obiekcie, tworzymy jego nową wersję. To podstawa nowoczesnego programowania.
2. **`viewModelScope.launch`**: Operacje na dysku (baza danych) muszą dziać się poza głównym wątkiem, żeby telefon nie przestał reagować na dotyk.

### UI w Jetpack Compose:
```kotlin
val streak by viewModel.streak.collectAsState()
```
`collectAsState()` to magiczny most – mówi Compose: "Ilekroć ta wartość w ViewModelu się zmieni, narysuj ten mały fragment ekranu od nowa". To podejście **reaktywne** – nie mówimy "zmień kolor tekstu", tylko mówimy "tekst zależy od tej zmiennej". Reszta dzieje się sama.
