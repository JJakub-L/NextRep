# Analiza Technologiczna Projektu NextRep

Niniejszy dokument wyjaśnia wybory technologiczne dokonane w projekcie oraz przybliża fundamenty języka Kotlin użytego do budowy aplikacji.

---

## 1. Wybór Środowiska i Języka

### Środowisko: Android Studio
Wybrano **Android Studio** jako główne środowisko programistyczne (IDE), ponieważ:
- Jest to oficjalne narzędzie wspierane przez Google.
- Posiada wbudowane narzędzia do podglądu interfejsu (Compose Preview).
- Oferuje zaawansowany debugger i analizator wydajności (Profiler).

### Język: Kotlin
Zastosowano język **Kotlin** (wersja 2.2.10) z następujących powodów:
- **Bezpieczeństwo (Null Safety):** Kotlin eliminuje większość błędów typu "NullPointerException".
- **Zwięzłość:** Pozwala pisać mniej kodu w porównaniu do Javy.
- **Wsparcie dla Coroutines:** Umożliwia asynchroniczne operacje na bazie danych w sposób czytelny i wydajny.

---

## 2. Trwałość Danych i Asynchroniczność

### A. Room Persistence Library
Aplikacja wykorzystuje bibliotekę **Room** do zarządzania lokalną bazą danych SQLite.
- **DAO (Data Access Object):** Interfejs `WorkoutDao` definiuje operacje na danych. Używamy `suspend fun` dla operacji zapisu/usuwania, co gwarantuje, że nie zablokujemy głównego wątku UI.
- **Flow:** Zapytania pobierające dane zwracają `Flow<List<Workout>>`. Jest to strumień danych, który automatycznie emituje nową listę za każdym razem, gdy dane w bazie ulegną zmianie.

### B. Kotlin Symbol Processing (KSP)
Zamiast starszego narzędzia kapt, projekt wykorzystuje **KSP**. Jest to nowocześniejszy i znacznie szybszy procesor adnotacji, który generuje kod implementacji bazy danych Room podczas kompilacji.

### C. Coroutines (Strumienie i Asynchroniczność)
W `WorkoutViewModel` używamy `viewModelScope.launch`, aby wykonywać operacje na bazie danych (zapis/usuwanie) w tle. Dzięki temu aplikacja pozostaje responsywna nawet podczas przetwarzania dużych planów treningowych.

---

## 3. Podstawy Języka Kotlin w Projekcie

### Tworzenie Zmiennych
- `val` (Value): Stała (np. `id` w encji Room).
- `var` (Variable): Zmienna (np. pola w `ExerciseSet`, które użytkownik edytuje podczas treningu).

### System Stanów w Compose
- **StateFlow:** Służy do przechowywania aktualnego stanu planów treningowych w ViewModelu.
- **collectAsState():** Funkcja w Composable, która przekształca `Flow` w `State` zrozumiały dla Compose, wymuszając odświeżenie ekranu po zmianie danych w bazie.

---

## 4. Zaawansowane Mechanizmy

### A. Data Classes & Room Entities
Klasa `Workout` jest jednocześnie klasą danych Kotlina oraz encją bazy danych (`@Entity`). Pozwala to na uniknięcie duplikacji modeli i łatwe przesyłanie danych między warstwami aplikacji.

### B. Type Converters
Ponieważ SQLite nie obsługuje natywnie list (np. listy ćwiczeń wewnątrz treningu), używamy `Converters.kt`. Wykorzystują one bibliotekę **GSON** do zamiany obiektów na tekst (JSON) przed zapisem do bazy i z powrotem na obiekty przy odczycie.

### C. Wzorzec Strategia (Strategy Pattern)
Logika punktacji treningu jest odseparowana od reszty kodu. Dzięki temu możemy łatwo dodać nowe sposoby oceny postępów (np. punkty za regularność vs punkty za rekordy siłowe).

---

## 5. Różnice między Kotlin a C# (Aktualizacja)

| Cecha | Kotlin | C# |
| :--- | :--- | :--- |
| **Asynchroniczność** | Coroutines (`suspend`) | `async` / `await` |
| **Strumienie danych** | `Flow` / `SharedFlow` | `IObservable` / `Channels` |
| **Baza danych** | Room (Annotation Processing) | Entity Framework Core |
| **Półśredniki** | Opcjonalne | Wymagane |
