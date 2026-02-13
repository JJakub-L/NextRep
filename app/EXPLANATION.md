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

## 3. Zarządzanie Stanem i UI (Jetpack Compose)

### A. System Stanów w Composable
- **StateFlow:** Służy do przechowywania aktualnego stanu planów treningowych w ViewModelu.
- **collectAsState():** Funkcja w Composable, która przekształca `Flow` w `State` zrozumiały dla Compose, wymuszając odświeżenie ekranu po zmianie danych w bazie.

### B. Stabilność Formularzy (TextFieldValue)
W celu uniknięcia problemu "skaczącego kursora" podczas reaktywnej aktualizacji bazy danych, zastosowano `TextFieldValue`. Pozwala to na oddzielenie stanu tekstu od stanu zaznaczenia i pozycji kursora, co zapewnia płynne wprowadzanie danych przez użytkownika.

### C. Reaktywne Podsumowania
Zastosowano mechanizm `remember(set.id)`, który gwarantuje, że stan lokalny pola tekstowego jest synchronizowany z modelem danych tylko wtedy, gdy faktycznie zmienia się kontekst serii treningowej.

---

## 4. Zaawansowane Mechanizmy Logiczne

### A. Obliczanie PR (Personal Record)
Logika statystyk opiera się na funkcji `getBestVolumeInPeriod`, która przeszukuje historyczne dane treningowe w określonych oknach czasowych (dziś, tydzień temu, miesiąc temu). Pozwala to na dynamiczne generowanie kart postępów bez konieczności tworzenia ciężkich zapytań SQL.

### B. Wzorzec Strategia (Strategy Pattern)
Logika punktacji treningu jest odseparowana od reszty kodu. Dzięki temu możemy łatwo dodać nowe sposoby oceny postępów (np. punkty za regularność vs punkty za rekordy siłowe).

### C. Automatyzacja Planu (WARMUP Logic)
ViewModel automatycznie modyfikuje strukturę nowo tworzonych treningów, wstrzykując serie typu `WARMUP` do pierwszego ćwiczenia. Jest to zaimplementowane przy użyciu operacji na kolekcjach (`mapIndexed`), co separuje logikę "przygotowania treningu" od czystych modeli danych.

---

## 5. Różnice między Kotlin a C#

| Cecha | Kotlin | C# |
| :--- | :--- | :--- |
| **Asynchroniczność** | Coroutines (`suspend`) | `async` / `await` |
| **Strumienie danych** | `Flow` / `SharedFlow` | `IObservable` / `Channels` |
| **Baza danych** | Room (Annotation Processing) | Entity Framework Core |
| **Półśredniki** | Opcjonalne | Wymagane |
