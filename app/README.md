# Dokumentacja Projektu NextRep

## 1. Cel Projektu
NextRep to nowoczesna aplikacja do śledzenia treningów siłowych, skupiająca się na prostocie obsługi podczas treningu oraz możliwości elastycznego planowania sesji i analizy progresji siłowej.

## 2. Architektura
Projekt realizuje zasady **Clean Architecture**, co zapewnia separację obaw (Separation of Concerns) i ułatwia testowanie oraz rozwój:
- **Data Layer**: Implementacja mechanizmów trwałości (Room), DAO oraz konwerterów. Odpowiada za dostarczanie danych do wyższych warstw.
- **Domain Layer**: Serce aplikacji zawierające logikę biznesową (np. strategie punktacji), interfejsy repozytoriów oraz modele domenowe (`Workout`, `Exercise`).
- **Presentation Layer**: Reaktywny interfejs użytkownika zbudowany w **Jetpack Compose** oraz **ViewModels** zarządzające stanem UI.

---

## 3. Struktury Danych i Trwałość

### A. Workout (Trening/Plan) - Encja Room
- **Model:** Klasa danych `@Entity` reprezentująca schemat tabeli `workout_plans`.
- **Relacje:** Przechowuje listy ćwiczeń i zbiory dni za pomocą serializacji JSON (GSON), co pozwala na zachowanie elastycznej struktury obiektowej w płaskiej bazie SQLite.

### B. Exercise & ExerciseSet
- Reprezentują hierarchiczną naturę treningu (Trening -> Ćwiczenie -> Serie).
- Obsługują polimorfizm funkcjonalny: różne typy ćwiczeń (`REPS_AND_WEIGHT`, `TIME`) współdzielą ten sam model danych.

---

## 4. Kluczowe Wzorce Projektowe (Design Patterns)

W projekcie świadomie zastosowano zestaw wzorców, aby zapewnić czystość kodu i skalowalność:

### A. Wzorzec Strategia (Strategy Pattern)
- **Problem:** Różne sposoby oceniania treningu (np. objętość vs. intensywność).
- **Rozwiązanie:** Interfejs `ScoringStrategy` definiuje kontrakt, a konkretne klasy (np. `VolumeStrategy`) implementują algorytm.
- **Zaleta:** Możemy dodać nową strategię punktacji bez modyfikacji klasy `WorkoutViewModel` (zasada Open-Closed).

### B. Wzorzec Obserwator (Observer Pattern)
- **Implementacja:** Wykorzystanie `Kotlin Flows` oraz `StateFlow`.
- **Działanie:** ViewModel "emituje" zmiany stanu, a UI (Compose) "subskrybuje" je poprzez `collectAsState()`.
- **Zaleta:** UI zawsze odzwierciedla aktualny stan danych bez potrzeby ręcznego odświeżania widoków.

### C. Wzorzec DAO (Data Access Object)
- **Zastosowanie:** `WorkoutDao` jako warstwa abstrakcji nad SQL.
- **Działanie:** Izoluje resztę aplikacji od szczegółów implementacyjnych SQLite i biblioteki Room.

### D. Wzorzec Adapter (Type Converters)
- **Zastosowanie:** Klasa `Converters`.
- **Działanie:** Adaptuje złożone obiekty Kotlinowe (Listy, Set-y) do formatu bazy danych (String/JSON), umożliwiając Roomowi obsługę nieobsługiwanych natywnie typów.

### E. Unidirectional Data Flow (UDF)
- **Model:** Zdarzenia (Events) płyną z UI do ViewModelu, a Stan (State) płynie z ViewModelu do UI.
- **Zaleta:** Pojedyncze źródło prawdy (Single Source of Truth) i łatwiejsze debugowanie stanów interfejsu.

---

## 5. System Postępów i Analityka
- **Personal Records (PR):** Algorytm `getBestVolumeInPeriod` dynamicznie wylicza rekordy życiowe dla konkretnych przedziałów czasowych.
- **Weekly Analytics:** Wizualizacja `Total Volume` za pomocą wykresów słupkowych, dająca użytkownikowi natychmiastową informację o trendach w jego aktywności.

---

## 6. Status Rozwoju
- [x] Implementacja Room z obsługą KSP.
- [x] Architektura MVI/UDF w warstwie prezentacji.
- [x] System "Streak" z inteligentną obsługą Rest Days.
- [x] Zaawansowany ekran postępów z wykresami i PR-ami.
- [x] Mechanizm stabilnych pól tekstowych (`TextFieldValue`).
- [x] Automatyczne preserie rozgrzewkowe (WARMUP logic).
- [x] System zarządzania planami (Dodawanie, Edycja, Usuwanie z potwierdzeniem).
