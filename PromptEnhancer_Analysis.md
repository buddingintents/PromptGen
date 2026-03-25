# Prompt Enhancer — Optimized Project Plan
**Prepared by:** Claude (AI Architect Review)  
**Original Author:** Ankit Sharma, Budding Intents  
**Date:** March 25, 2026  
**Version:** 2.0 (Vibe-Coding Optimized)

---

## STEP 1: STRUCTURED ANALYSIS

### 1.1 Core Idea & Product Vision

**What it is:** A Flutter mobile app that takes a raw user prompt, detects its topic using an LLM, refines it into a higher-quality prompt, and returns the improved version — along with history, usage metrics, trending topics, and multi-provider LLM support.

**Vision gap in original plan:** The plan describes *how to build* but never defines *why a user opens this app* over just using the LLM directly. The product vision needs sharpening: the app's core value is **"turn a mediocre prompt into a great one, on any LLM, in one tap."**

---

### 1.2 Target Users & Use Cases

| User Type | Primary Use Case |
|---|---|
| AI power users | Refine prompts before pasting into ChatGPT/Claude |
| Developers | Batch-refine system prompts for their own apps |
| Non-technical users | Get better results from AI without learning prompt engineering |
| Content creators | Improve writing/image prompts quickly |

**Missing from original plan:** No user persona definition. No onboarding flow. No empty-state design guidance. These are not optional in a consumer app.

---

### 1.3 Feature Breakdown

#### Core (MVP — must ship)
- Prompt input + topic detection
- Prompt refinement via LLM
- Provider selection (OpenAI, Gemini, Claude)
- API key management (secure storage)
- Basic history (view, copy, re-run)

#### Important (v2)
- History filters (topic, date, provider)
- Metrics dashboard (tokens, latency, daily usage)
- Light/dark theme toggle

#### Optional / Nice-to-have (v3+)
- Trending topics (local analysis)
- Language selection (en/hi)
- HuggingFace + Perplexity provider support
- Firebase sync placeholder

---

### 1.4 Tech Stack Evaluation

| Dependency | Assessment | Verdict |
|---|---|---|
| `flutter_riverpod` | Correct choice for state management | ✅ Keep |
| `go_router` | Correct choice for deep linking | ✅ Keep |
| `hive` + `hive_flutter` | Good for local storage | ✅ Keep |
| `flutter_secure_storage` | Correct for API keys | ✅ Keep |
| `dio` | Over-engineered for this use case | ⚠️ Consider `http` package first; add Dio only if interceptors needed |
| `freezed` + `json_serializable` | Adds significant boilerplate + build_runner overhead | ⚠️ Use only for domain models, not every class |
| `fl_chart` | Good for metrics | ✅ Keep |
| `build_runner` | Mandatory if using freezed | ✅ Keep (but minimize usage) |

**Missing dependencies:**
- `flutter_dotenv` or similar for default config
- `share_plus` (users will want to share refined prompts)
- `connectivity_plus` (offline detection)
- `package_info_plus` (version in settings screen)

---

### 1.5 Architecture Pattern Assessment

The original plan correctly proposes Clean Architecture (data/domain/presentation) with feature-first folders. This is **valid but over-engineered for a solo/small-team vibe-coding project.**

**Issues with the original architecture:**
- Clean Architecture adds 3x more files per feature — for an MVP this slows iteration
- `UseCase` classes for simple CRUD (SaveHistoryUseCase, GetHistoryUseCase) add no value over calling the repository directly from the controller
- The 12-step Codex prompt sequence has too many interdependencies — Step 6 requires Steps 3, 4, and 5 to already be complete

**Recommendation:** Use **Simplified Clean Architecture** — keep feature-first folders but collapse data+domain into a single `repository` layer for non-complex features. Apply full Clean Architecture only where business logic is genuinely complex (the LLM provider abstraction).

---

### 1.6 Development Approach Issues

| Issue | Risk Level | Description |
|---|---|---|
| No splash/onboarding defined | Medium | First-run experience missing entirely |
| No API key validation flow | High | Users will enter wrong keys with no feedback |
| Biometric check for copy (Step 8) | Medium | "Mock if needed" is a bad signal — define a clear decision |
| Firebase placeholder in v1 | Low | Adds mental overhead; defer completely |
| `flutter_secure_storage` on all platforms | Medium | Needs keychain entitlements on iOS — not mentioned |
| No error UI patterns | High | What does the user see when the LLM API fails? |
| No loading skeleton | Medium | App will feel broken during first prompt refinement |

---

### 1.7 Missing Components

- Onboarding / first-run setup screen (enter first API key)
- API key validation (ping the provider before saving)
- Prompt copy button (most common action after refinement)
- Share sheet integration
- Rate limiting / retry logic for LLM calls
- Offline mode indicator
- App version display in Settings

---

## STEP 2: VIBE CODING OPTIMIZATION

### Principles Applied

1. **One AI prompt = one file or one self-contained function**
2. **Never generate more than ~150 lines in a single prompt**
3. **Generate stubs first, implement second** — this lets you validate structure before logic
4. **Test the happy path before error handling** — do not add error states in the same prompt as feature logic
5. **State before UI** — always generate the Riverpod provider/controller before generating the widget

### Atomic Task Breakdown

Each task below is independently executable via a single Codex/GPT prompt:

| # | Task | Lines Est. | Depends On |
|---|---|---|---|
| T01 | pubspec.yaml + cleaned main.dart | 50 | — |
| T02 | Folder structure (empty dart files) | — | T01 |
| T03 | AppTheme (colors, text styles, Material 3) | 80 | T01 |
| T04 | GoRouter setup with stub screens | 60 | T02 |
| T05 | SecureStorage wrapper | 40 | T02 |
| T06 | Hive setup + base StorageService | 60 | T02 |
| T07 | Dio client + interceptors | 70 | T02 |
| T08 | AppException + error mapper | 40 | T02 |
| T09 | LLMService abstract interface + LLMResponse model | 50 | T08 |
| T10 | OpenAIService implementation | 80 | T09 |
| T11 | GeminiService implementation | 80 | T09 |
| T12 | ClaudeService implementation | 80 | T09 |
| T13 | LLMProviderRepository (selects active service) | 60 | T10–T12 |
| T14 | PromptController (Riverpod Notifier) + PromptState | 80 | T13 |
| T15 | PromptScreen UI (input + refine button + output card) | 120 | T14 |
| T16 | HistoryItem Hive model + adapter | 50 | T06 |
| T17 | HistoryRepository (save/get/delete) | 60 | T16 |
| T18 | HistoryController + HistoryState | 60 | T17 |
| T19 | HistoryScreen UI | 100 | T18 |
| T20 | SettingsController + provider storage | 70 | T05 |
| T21 | SettingsScreen UI (API key management) | 100 | T20 |
| T22 | MetricsRepository (compute from history) | 60 | T17 |
| T23 | MetricsScreen UI (fl_chart) | 120 | T22 |
| T24 | TrendingRepository (local topic analysis) | 50 | T17 |
| T25 | TrendingScreen UI | 80 | T24 |
| T26 | Shared widgets (AppButton, AppCard, AppTextField) | 100 | T03 |
| T27 | Loading + error + empty state widgets | 60 | T26 |
| T28 | Unit tests: PromptController | 80 | T14 |
| T29 | Unit tests: LLMService (mock) | 60 | T09 |
| T30 | Integration pass: wire all screens into router | 40 | All |

---

## STEP 3: MODULAR ARCHITECTURE

### Simplified Clean Architecture (Vibe-Coding Edition)

```
lib/
├── main.dart
├── app.dart
│
├── core/
│   ├── network/
│   │   ├── dio_client.dart          # Dio instance + interceptors
│   │   └── api_exception.dart       # AppException + mapper
│   ├── storage/
│   │   ├── hive_service.dart        # Hive init + box access
│   │   └── secure_storage.dart      # flutter_secure_storage wrapper
│   ├── theme/
│   │   ├── app_theme.dart           # Material 3 ThemeData (light + dark)
│   │   └── app_colors.dart          # Color constants
│   └── constants/
│       └── app_constants.dart       # Timeout, box names, etc.
│
├── llm/                             # Shared LLM abstraction (used by features)
│   ├── llm_service.dart             # abstract interface + LLMResponse
│   ├── providers/
│   │   ├── openai_service.dart
│   │   ├── gemini_service.dart
│   │   └── claude_service.dart
│   └── llm_provider_repository.dart # Picks active provider at runtime
│
├── features/
│   ├── prompt/
│   │   ├── data/
│   │   │   └── prompt_repository.dart
│   │   ├── presentation/
│   │   │   ├── prompt_controller.dart   # Riverpod Notifier
│   │   │   ├── prompt_state.dart
│   │   │   └── prompt_screen.dart
│   │   └── widgets/
│   │       ├── prompt_input_card.dart
│   │       └── refined_output_card.dart
│   │
│   ├── history/
│   │   ├── data/
│   │   │   ├── history_item.dart        # Hive model
│   │   │   └── history_repository.dart
│   │   ├── presentation/
│   │   │   ├── history_controller.dart
│   │   │   ├── history_state.dart
│   │   │   └── history_screen.dart
│   │   └── widgets/
│   │       └── history_list_tile.dart
│   │
│   ├── settings/
│   │   ├── data/
│   │   │   └── settings_repository.dart
│   │   ├── presentation/
│   │   │   ├── settings_controller.dart
│   │   │   └── settings_screen.dart
│   │   └── widgets/
│   │       └── api_key_tile.dart
│   │
│   ├── metrics/
│   │   ├── data/
│   │   │   └── metrics_repository.dart  # Derives from history
│   │   └── presentation/
│   │       └── metrics_screen.dart
│   │
│   └── trending/
│       ├── data/
│       │   └── trending_repository.dart
│       └── presentation/
│           └── trending_screen.dart
│
└── shared/
    └── widgets/
        ├── app_button.dart
        ├── app_card.dart
        ├── app_text_field.dart
        ├── loading_indicator.dart
        ├── error_state_widget.dart
        └── empty_state_widget.dart
```

### Module Boundary Rules

- **`core/`** — zero Flutter UI imports, only dart/flutter_foundation
- **`llm/`** — no Riverpod providers; returns plain Dart objects only
- **`features/*/data/`** — no BuildContext, no widgets
- **`features/*/presentation/`** — contains Riverpod providers and screens only
- **`shared/widgets/`** — stateless/stateful widgets only, no business logic

---

## STEP 4: AI-FIRST EXECUTION PLAN

### Phase 0 — Foundation (T01–T08)

---

**T01 — Project Bootstrap**

**Goal:** Clean pubspec.yaml and empty main.dart

**Prompt:**
```
Create a Flutter pubspec.yaml for a project named "prompt_enhancer".
Add these dependencies: flutter_riverpod ^2.x, go_router ^14.x, hive ^2.x,
hive_flutter, flutter_secure_storage, dio, freezed_annotation, json_annotation, fl_chart, share_plus, connectivity_plus.
Dev dependencies: build_runner, freezed, json_serializable.
Also produce a main.dart that only calls runApp(ProviderScope(child: App())).
Output ONLY the two files. No explanations.
```

**Expected Output:** `pubspec.yaml`, `main.dart`  
**Validation:** `flutter pub get` runs clean, no analysis errors

---

**T02 — Folder Structure**

**Goal:** All empty .dart files with correct class stubs

**Prompt:**
```
Given this Flutter folder structure: [paste structure from Step 3]
Generate every .dart file listed with ONLY a stub:
- For a class file: class ClassName {}
- For an abstract file: abstract class InterfaceName {}
- For a screen: class XScreen extends ConsumerWidget { @override Widget build(...) => Scaffold(); }
Do NOT add any logic. Output as a flat list: filename → content.
```

**Expected Output:** 30+ stub dart files  
**Validation:** All files compile with `flutter analyze`

---

**T03 — AppTheme**

**Goal:** Material 3 light + dark theme with brand colors

**Prompt:**
```
Create app_theme.dart for a Flutter app. Use Material 3.
Brand colors: primary #1A73E8 (blue), surface #F8F9FA, error #EA4335.
Export:
- AppTheme.light → ThemeData
- AppTheme.dark → ThemeData
Include: colorScheme, textTheme (bodyLarge/bodyMedium/titleLarge), inputDecorationTheme, elevatedButtonTheme.
No widgets, no Riverpod. Pure ThemeData.
```

**Validation:** Reference in `app.dart`, run on simulator — both themes render correctly

---

**T04 — GoRouter + ProviderScope**

**Goal:** Named routes wired to stub screens

**Prompt:**
```
Create app_router.dart for Flutter using go_router inside a Riverpod provider.
Routes (all use stub ConsumerWidget screens already created):
- /splash → SplashScreen
- / → PromptScreen (shell with bottom nav)
- /history → HistoryScreen
- /trending → TrendingScreen
- /metrics → MetricsScreen
- /settings → SettingsScreen
Use ShellRoute for bottom navigation on the 5 main screens.
The router must be a Riverpod Provider<GoRouter>.
Output ONLY app_router.dart.
```

**Validation:** All 6 routes navigate without errors

---

**T05–T08 — Core Infrastructure**

Run these 4 prompts sequentially. Each references only files already created.

**T05 Prompt:**
```
Create secure_storage.dart — a wrapper around flutter_secure_storage.
Class: SecureStorageService
Methods: Future<void> write(String key, String value), Future<String?> read(String key), Future<void> delete(String key), Future<Map<String, String>> readAll()
No Riverpod. Plain class. Include a Riverpod Provider at the bottom of the file.
```

**T06 Prompt:**
```
Create hive_service.dart for Flutter.
Static method: HiveService.init() — opens boxes: 'history', 'settings'
Static method: HiveService.box<T>(String name) → Box<T>
Call HiveService.init() in main.dart before runApp.
```

**T07 Prompt:**
```
Create dio_client.dart.
Create a Dio instance with: baseUrl as empty string (set per-call), connectTimeout 10s, receiveTimeout 15s.
Add LogInterceptor (NO request headers logged — may contain API keys).
Add an interceptor that catches DioException and throws AppException.
Export as a Riverpod Provider<Dio>.
```

**T08 Prompt:**
```
Create api_exception.dart.
sealed class AppException with cases:
- NetworkException(String message)
- TimeoutException()
- AuthException()  
- ServerException(int statusCode, String message)
- UnknownException(String message)
Also create a static ApiErrorMapper.fromDioException(DioException e) → AppException.
```

---

### Phase 1 — LLM Layer (T09–T13)

---

**T09 — LLM Interface**

**Prompt:**
```
Create llm_service.dart.
Define:

class LLMResponse {
  final String text;
  final int tokens;
  final String provider;
  final int latencyMs;
}

abstract class LLMService {
  String get providerName;
  Future<LLMResponse> refinePrompt(String input);
  Future<String> detectTopic(String input);
}

No implementations. No Riverpod. Export both.
```

---

**T10 — OpenAI Implementation**

**Prompt:**
```
Create openai_service.dart implementing LLMService.
Constructor: OpenAIService({required Dio dio, required String apiKey, String model = 'gpt-4o-mini'})
refinePrompt: POST https://api.openai.com/v1/chat/completions
System prompt: "You are a prompt engineering expert. Rewrite the user's prompt to be clearer, more specific, and more effective for AI systems. Return ONLY the improved prompt."
detectTopic: Same endpoint, system prompt: "Classify this prompt into one topic word. Return ONLY the topic word, no punctuation."
Parse response.choices[0].message.content.
Track latency using Stopwatch.
Throw AppException on failure.
```

---

**T11, T12** — Repeat same pattern for GeminiService and ClaudeService with correct API endpoints and payload shapes.

---

**T13 — Provider Repository**

**Prompt:**
```
Create llm_provider_repository.dart.
Class: LLMProviderRepository
Constructor takes SecureStorageService.
Method: Future<LLMService> getActiveService(Dio dio)
- Reads 'active_provider' from secure storage (default: 'openai')
- Reads corresponding API key (e.g., 'openai_api_key')
- Returns the correct LLMService implementation
- Throws AppException.AuthException if no key found
Expose as Riverpod AsyncNotifier.
```

---

### Phase 2 — Features (T14–T25)

#### Prompt Feature (T14–T15)

**T14 Prompt:**
```
Create prompt_state.dart and prompt_controller.dart.

PromptState (freezed):
- input: String
- topic: String?
- refinedOutput: String?
- isLoading: bool
- error: String?

PromptController extends AsyncNotifier<PromptState>:
- setInput(String)
- refine() — calls detectTopic then refinePrompt, updates state
- clear()

Use LLMProviderRepository to get the active service.
Handle AppException and map to state.error.
```

**T15 Prompt:**
```
Create prompt_screen.dart.
Layout (vertical scroll, single column):
- AppTextField (multi-line, "Enter your prompt...")
- Row: topic Chip (shown when topic != null) + "Refine" AppButton
- When loading: CircularProgressIndicator centered
- When refinedOutput != null: AppCard showing refined text + Copy IconButton + Share IconButton
- When error != null: ErrorStateWidget showing error message + retry
Watch PromptController. No business logic in widget.
```

---

#### History Feature (T16–T19)

**T16 Prompt:**
```
Create history_item.dart.
@HiveType() class HistoryItem:
- id: String (uuid)
- originalPrompt: String
- refinedPrompt: String
- topic: String
- provider: String
- tokens: int
- latencyMs: int
- createdAt: DateTime
Register adapter. Export HiveAdapter.
```

**T17 Prompt:**
```
Create history_repository.dart.
Class HistoryRepository:
- Uses HiveService.box<HistoryItem>('history')
- save(HistoryItem item)
- List<HistoryItem> getAll() — sorted by createdAt desc
- delete(String id)
- clear()
Expose as Riverpod Provider.
```

**T18–T19:** Generate HistoryController (with filter state: selectedTopic, dateRange) and HistoryScreen (ListView + filter row + swipe-to-delete).

---

#### Settings Feature (T20–T21)

**T20 Prompt:**
```
Create settings_repository.dart and settings_controller.dart.
SettingsRepository (uses SecureStorageService):
- Future<void> saveApiKey(String provider, String key)
- Future<String?> getApiKey(String provider)
- Future<void> deleteApiKey(String provider)
- Future<void> setActiveProvider(String provider)
- Future<String> getActiveProvider()

SettingsController (Riverpod AsyncNotifier):
- Loads all saved providers on init
- exposes saveKey, deleteKey, setActive methods
- State: Map<String, bool> providerConfigured, String activeProvider
```

**T21 Prompt:**
```
Create settings_screen.dart.
Sections:
1. LLM Providers — list of providers (OpenAI, Gemini, Claude). Each row: provider name + status chip (configured/not) + edit icon.
   Tapping edit opens a BottomSheet with an AppTextField (obscured, for API key) + Save button.
2. Appearance — theme toggle: System / Light / Dark (use SharedPreferences via Riverpod).
3. About — app version (use package_info_plus).
No business logic in widget. All via SettingsController.
```

---

#### Metrics & Trending (T22–T25)

**T22 Prompt:**
```
Create metrics_repository.dart.
Derives all metrics from HistoryRepository. No separate storage.
Compute:
- Map<String, int> tokensByProvider
- Map<String, double> avgLatencyByProvider  
- Map<String, int> promptsPerDay (last 14 days, keyed by 'yyyy-MM-dd')
Returns MetricsSummary data class. Expose as Riverpod FutureProvider.
```

**T23 Prompt:**
```
Create metrics_screen.dart.
Uses fl_chart.
Show:
- BarChart: tokens per provider
- LineChart: prompts per day (last 14 days)
- Row of stat cards: total prompts, avg latency, top provider
Use LoadingIndicator while data loads. Use EmptyStateWidget if no history.
```

**T24–T25:** TrendingRepository (group history by topic, count last 7 days) and TrendingScreen (GridView of topic cards with count badges, tap to copy to clipboard).

---

### Phase 3 — Polish & Tests (T26–T30)

**T26 Prompt:**
```
Create shared widgets in lib/shared/widgets/:
AppButton: ElevatedButton wrapper, params: label, onPressed, isLoading (shows spinner inside button), width.
AppCard: Card wrapper, params: child, padding (default 16), optional title String.
AppTextField: TextFormField wrapper, params: hint, controller, maxLines, obscure, onChanged.
All use theme colors. No hardcoded colors.
```

**T27 Prompt:**
```
Create error and empty state widgets.
LoadingIndicator: centered CircularProgressIndicator with optional label.
ErrorStateWidget: icon + message + optional retry ElevatedButton.
EmptyStateWidget: icon + title + subtitle. Params: icon, title, subtitle, optional action button.
All stateless. All use theme.
```

**T28–T29:** Unit tests for PromptController (mock LLMService using Mockito) and LLMService interface contract tests.

**T30 Prompt:**
```
Review app.dart and app_router.dart.
Wire BottomNavigationBar with 5 items: Prompt, History, Trending, Metrics, Settings.
Ensure active route highlights correct nav item.
Ensure ProviderScope wraps MaterialApp.router.
Theme reads from a themeProvider (Riverpod StateProvider<ThemeMode>).
```

---

## STEP 5: ITERATION STRATEGY

### MVP (v0.1) — Core Loop Only
**Build order:** T01 → T08 → T09 → T12 → T13 → T14 → T15 → T20 → T21 → T26 → T30

**What ships:**
- One-screen app: enter prompt → select provider → tap Refine → see result → copy
- API key management in settings
- Basic navigation shell

**What's excluded:** History, Metrics, Trending, charts, theme toggle

**Target:** Working demo in 2–3 days of vibe coding

---

### v0.2 — History & Persistence
Add T16 → T19. Every refinement is now auto-saved. History screen is browsable.

---

### v0.3 — Metrics & Trending
Add T22 → T25. App now shows usage patterns. Trending topics are surfaced.

---

### v1.0 — Polish & Tests
Add T26–T30. Full widget polish, error states, loading states, unit tests.

---

### Feedback Loop Per Cycle
1. Build feature → run on simulator
2. Test happy path manually (1 minute)
3. Test error path (no API key, network off)
4. Fix regressions before moving to next task
5. Commit after each task (atomic git commits)

---

### Refactor vs Extend Decision Rule
- **Refactor** when: a controller exceeds 150 lines, a screen has inline business logic, the same Dio call appears in 2+ places
- **Extend** when: adding a new LLM provider, adding a new metric, adding a new filter to history

---

## STEP 6: PERFORMANCE & SCALING

### Local Performance
- **Hive reads are synchronous** — keep box sizes small. Archive history older than 90 days.
- **LLM calls are async** — always show loading state. Never block UI thread.
- **fl_chart can be slow** with large datasets — limit metrics to last 14 days in the query, not in the widget.
- **Riverpod rebuilds** — use `select()` to prevent full widget rebuilds when only part of the state changes.

### Caching Strategy
- Cache the last refined prompt result in PromptController state so navigating away and back doesn't re-trigger the API call.
- Cache metrics computation result for 5 minutes using Riverpod's `cacheFor` (available in Riverpod 2.x with `keepAlive`).

### Offline Capability
- Detect connectivity with `connectivity_plus` at app start.
- Show a non-blocking banner ("You're offline — history available") when offline.
- History and metrics screens work fully offline.
- Prompt screen shows a clear disabled state with message when offline.

### Future Backend Scaling (if cloud sync added)
- Use Firestore for sync (not RTDB — document model fits history items well)
- Index by `userId + createdAt` for paginated history queries
- Never store API keys server-side — keep them device-only

---

## STEP 7: TESTING & DEBUG STRATEGY

### Unit Test Approach

| Module | Test Type | Mock Strategy |
|---|---|---|
| LLMService implementations | Contract tests | Mock Dio responses using `mockito` |
| PromptController | State transition tests | Mock LLMService |
| HistoryRepository | CRUD tests | Use `hive_test` in-memory box |
| SettingsController | Key storage tests | Mock SecureStorageService |
| MetricsRepository | Computation tests | Seed fake history, assert metrics |

### Test-First Rule for AI Generation

When asking AI to generate a controller or repository, **always ask for tests in the same prompt** or immediately after. Separating them by days means you'll never write them.

**Example:**
```
After generating PromptController, immediately prompt:
"Now write unit tests for PromptController using flutter_test and mockito.
Test: initial state, successful refine flow, error handling when LLM throws AppException."
```

### AI Debugging Workflow

When a Codex-generated piece of code fails:
1. Copy the error message verbatim into a new prompt
2. Include the failing file + the error
3. Prompt: *"This Flutter code throws [error]. Here is the file: [file]. Fix ONLY the error, do not refactor anything else."*
4. Never ask AI to "rewrite" when "fix" will do — rewrites break working code

### Common Vibe Coding Failure Patterns

| Pattern | Symptom | Prevention |
|---|---|---|
| Monolithic generation | 300-line file that breaks in 5 places | Keep prompts to one class/function |
| Context bleed | AI uses wrong class names from earlier files | Always include the relevant existing file in the prompt |
| State explosion | Controller with 12 fields in state | Split into sub-controllers per concern |
| Premature abstraction | UseCase wrapping a single repo call | Skip UseCases for CRUD; add when logic branches |
| Missing null checks | Null safety errors at runtime | Ask AI to "enable strict null safety and handle all nulls" |

---

## STEP 8: FINAL OUTPUT FORMAT

### 8.1 Improved Project Summary

**Prompt Enhancer** is a Flutter mobile app that helps users write better AI prompts. Given any rough input, the app detects its topic using an LLM, rewrites it into a higher-quality prompt, and returns the result in seconds. Users can save, re-run, and share past prompts. The app supports multiple LLM providers and stores API keys securely on-device. A metrics dashboard shows usage patterns over time.

---

### 8.2 Optimized Feature List (Prioritized)

| Priority | Feature | Justification |
|---|---|---|
| P0 | Prompt refinement (core loop) | This is the entire value proposition |
| P0 | API key management | App is useless without this |
| P0 | Provider selection (OpenAI, Gemini, Claude) | 3 providers covers 95% of users |
| P1 | History (save, view, copy, re-run) | Retention and utility |
| P1 | Copy + Share refined prompt | Core user action after refinement |
| P2 | Metrics dashboard | Power user feature |
| P2 | Trending topics | Engagement feature |
| P3 | Theme toggle | Polish |
| P3 | Language selection (en/hi) | Localization |
| Deferred | HuggingFace / Perplexity | Low user demand at MVP stage |
| Deferred | Firebase sync | Adds backend complexity; not needed for v1 |

---

### 8.3 Final Folder Structure

See Step 3 above. Key change from original: `llm/` is promoted to a top-level module (alongside `core/` and `features/`) because it is shared infrastructure, not a feature.

---

### 8.4 Step-by-Step AI Build Plan

See Step 4 above. 30 atomic tasks, each a single AI prompt. Build in order T01 → T30 with the MVP shortcut: T01–T15, T20–T21, T26, T30 for a working demo.

---

### 8.5 Iteration Roadmap

| Version | Scope | Est. Time |
|---|---|---|
| v0.1 MVP | Core loop + settings | 2–3 days |
| v0.2 | History | 1–2 days |
| v0.3 | Metrics + Trending | 1–2 days |
| v1.0 | Polish + tests + error states | 1–2 days |

---

### 8.6 Risks & Mitigation

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| LLM API key validation complexity | Medium | High | Validate key on save by making a cheap test call |
| iOS Keychain entitlements for secure_storage | Medium | High | Add entitlements.plist early (T05 phase) |
| build_runner slowness (freezed) | High | Medium | Only use freezed for PromptState + LLMResponse; use plain classes elsewhere |
| fl_chart API changes | Low | Medium | Pin fl_chart version |
| Riverpod provider overrides in tests | Medium | Medium | Use ProviderContainer in tests, not ProviderScope |
| Over-engineering LLM abstraction before validating core flow | Medium | High | Build OpenAI only first; abstract after v0.1 ships |

---

### 8.7 Optional Enhancements (Post v1.0)

- **Prompt templates:** Pre-built prompt structures for common tasks (write email, summarize, explain code)
- **Diff view:** Show what changed between original and refined prompt (highlight additions/removals)
- **Batch mode:** Refine multiple prompts in sequence
- **Widget/shortcut:** iOS/Android home screen widget to trigger refinement from clipboard
- **Export:** Export history as CSV or JSON
- **Community prompts:** Browse and fork prompts shared by others (requires backend)
- **Model parameter controls:** Temperature, max tokens exposed in advanced settings

---

*End of analysis. Total tasks: 30 atomic prompts. Estimated vibe-coding completion: 6–9 days for v1.0.*
