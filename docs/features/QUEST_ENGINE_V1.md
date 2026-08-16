# Quest Engine v1 — Workflow & UI Specification

## 1. Цель

Добавить полноценный базовый quest workflow:

```text
Discover
    ↓
Preview
    ↓
Accept
    ↓
Track
    ↓
Progress
    ↓
Ready to Turn In
    ↓
Complete / Reward
```

В первой реализации основной источник квестов — **Notice Board внутри Location Screen**.

Ключевой UX-принцип:

> Квестовая система не должна ощущаться как отдельное приложение или отдельная страница. Игрок взаимодействует с ней внутри игрового мира, при этом Global Shell остается стабильным.

---

## 2. Global Quest UI Rules

Во всех состояниях Quest workflow сохраняются:

- Top Bar;
- player avatar;
- player name;
- level / XP;
- currencies/resources;
- Left Navigation;
- Activity & Notifications;
- нижний Chat.

Не добавлять на Notice Board:

- Health;
- Mana;
- Stamina combat HUD;
- skill bar;
- отдельный нижний character portrait.

Player identity всегда остается в Top Bar.

Combat HUD используется только на экранах, где действительно происходит combat.

---

## 3. Chat

Для Notice Board workflow нижняя область полностью отдается чату.

Chat должен быть persistent:

```text
Step 1 → тот же Chat
Step 2 → тот же Chat
Accept → тот же Chat
```

Открытие preview не должно:

- перемещать Chat;
- менять его высоту;
- размонтировать его;
- сбрасывать выбранный channel;
- очищать сообщения.

Chat является частью Global Shell, а не Notice Board.

---

## 4. STEP 1 — Quest Discovery / Notice Board

**Смотри картинки: `quest-step-1-notice-board.png`**

### Trigger

Игрок находится в Location и нажимает:

```text
Notice Board
```

Не выполнять navigation на отдельный `/quests` screen.

Открыть Notice Board внутри текущего Location Screen.

### Layout

Сохраняются:

```text
Top Bar
Left Navigation
Location artwork
Activity & Notifications
Chat
```

Поверх правой части Location workspace появляется:

```text
NOTICE BOARD
AVAILABLE QUESTS
```

Location artwork остается видимым слева.

Это важно для ощущения:

> игрок все еще физически находится возле доски объявлений в Blackstone.

### Notice Board list

Каждая запись должна содержать минимум:

```text
Quest icon / crest
Quest Title
Quest Type
Recommended Level
Short description

Rewards preview:
XP
Currency
optional reward/resource
```

Пример:

```text
THE MISSING CARAVAN
Investigation
Level 4

A merchant caravan has vanished on the Old Road.
Find what happened to Varro's convoy.

320 XP
85 Silver
45 Marks
```

### Quest states в списке

Поддержать визуально:

```text
Available
Selected
Unavailable / level restricted
Already Active
Completed / unavailable
```

Не обязательно показывать все states в первой fake fixture, но компонент должен быть к ним готов.

### Filter

Notice Board поддерживает простой filter control.

Например:

```text
All
Combat
Exploration
Collection
PvP
```

На первом этапе filter может быть минимальным, но архитектуру не связывать жестко с четырьмя типами.

### UI state

Frontend должен иметь концептуально:

```text
NoticeBoardState:
CLOSED
LIST
PREVIEW
```

Step 1:

```text
LIST
selectedQuestId = null
```

---

## 5. STEP 1 → STEP 2 TRANSITION

Это принципиально важная часть workflow.

При клике на:

```text
The Missing Caravan
```

НЕ:

- открывать новую page;
- переносить quest list;
- менять весь Location layout;
- переставлять Activity;
- перестраивать Chat.

Вместо этого **тот же NoticeBoard component расширяется влево**.

### Spatial continuity

В Step 1:

```text
LOCATION ART        [ QUEST LIST ][ ACTIVITY ]
```

После выбора:

```text
LOCATION [ QUEST PREVIEW | QUEST LIST ][ ACTIVITY ]
```

Правый край Notice Board остается практически на том же месте.

Расширяется только левый край.

Таким образом пользователь визуально видит:

> Я открыл объявление на той же доске.

а не:

> Игра загрузила другую страницу.

### Animation

Ориентировочно:

```text
selected card highlight:
0–100ms

panel expand:
150–250ms

preview content fade/slide:
150–300ms
```

Использовать restrained easing.

Не использовать:

- zoom;
- bounce;
- large scaling;
- cinematic transitions.

---

## 6. STEP 2 — Quest Preview

**Смотри картинки: `quest-step-2-quest-preview.png`**

### Основной layout

Expanded Notice Board состоит из двух частей:

```text
┌─────────────────────────────────────────────┐
│ NOTICE BOARD                                │
├───────────────────────────┬─────────────────┤
│                           │ AVAILABLE       │
│ QUEST PREVIEW             │ QUESTS          │
│                           │                 │
│ ~60–65%                   │ ~35–40%         │
│                           │                 │
└───────────────────────────┴─────────────────┘
```

#### Слева

Selected quest details.

#### Справа

Тот же самый Quest List из Step 1.

---

## 7. Quest List не должен перемещаться

Это ключевой requirement.

На Step 1 list находится справа.

На Step 2 он **остается справа**.

Не делать старую схему:

```text
Step 1:
Location | Quest List

Step 2:
Quest List | Quest Preview
```

Это ломает visual continuity.

---

## 8. Quest Preview Content

Preview должен содержать:

```text
Quest Title
Quest Type
Recommended Level
Quest Artwork
Quest Giver
Description
Objectives
Location
Difficulty
Rewards
Accept Quest
Decline/Close
```

### Header

Например:

```text
THE MISSING CARAVAN

Side Quest
Recommended Level: 4
```

### Quest artwork

Использовать небольшой/средний сюжетный artwork.

Для примера:

```text
abandoned caravan on Old Road
```

Artwork должен поддерживать narrative, но не занимать половину всего экрана.

### Quest giver

Компактный блок:

```text
[portrait]

QUEST GIVER
Captain Varro
```

Не создавать огромную отдельную biography panel.

---

## 9. Objectives

Quest preview показывает ordered objective list.

Например:

```text
◇ Search the Old Road
◇ Investigate the Abandoned Wagon
◇ Defeat Roadside Bandits
◇ Return to Captain Varro
```

До принятия все значения:

```text
0/1
```

но это preview целей, не active progress.

---

## 10. Location & Difficulty

Компактный secondary block:

```text
LOCATION
Old Road
Blackstone Outskirts

DIFFICULTY
Easy
```

Difficulty не должна определяться только цветом.

---

## 11. Rewards

Использовать reusable reward presentation.

Например:

```text
[XP]      320 XP

[Silver]   85 Silver

[Item]    Caravan Supply Satchel
```

Quest engine должен поддерживать:

```text
XP
currency
resource
item
reputation — later
```

Не hardcode'ить UI исключительно под XP + Silver.

---

## 12. Switching quests inside Preview

Пока Preview открыт, игрок может нажать другой quest справа.

Например:

```text
The Missing Caravan
        ↓
Rat Problem
```

При этом:

- panel не закрывается;
- layout не меняется;
- Preview content обновляется;
- selected state переходит на новую запись.

Допустим небольшой content crossfade.

---

## 13. Decline / Close behavior

На этапе Preview квест еще не принят.

`Decline` фактически означает:

```text
close/collapse preview
```

а не backend quest rejection.

Можно назвать действие:

```text
Back
```

или:

```text
Close
```

если `Decline` создает ложное ощущение сохраненного решения.

---

## 14. Accept Quest

При нажатии:

```text
ACCEPT QUEST
```

frontend вызывает quest accept command.

Пока запрос выполняется:

```text
ACCEPT QUEST
↓
Accepting...
```

заблокировать повторный submit.

После success:

```text
Accepted ✓
```

---

## 15. STEP 2 → STEP 3 TRANSITION

Не делать full-page navigation.

После успешного Accept:

1. показать небольшой `Quest Accepted` feedback;
2. обновить active quest state;
3. закрыть / collapse Notice Board;
4. вернуть стандартный Location Screen;
5. показать Quest Tracker.

Пример последовательности:

```text
Accept Quest
    ↓
Quest Accepted ✓
    ↓
small toast / Activity update
    ↓
Notice Board closes
    ↓
Location
    ↓
Quest Tracker appears
```

---

## 16. STEP 3 — Active Quest / Tracker

**Смотри картинки: `quest-step-3-active-tracker.png`**

ВАЖНО:

Из этого mockup использовать:

- Quest Accepted feedback;
- Quest Tracker concept;
- active objective presentation.

Не использовать старый нижний combat HUD.

Global shell должен соответствовать:

**смотри картинки: `quest-step-1-notice-board.png`**

### Tracker

После Accept игрок должен видеть компактный tracker:

```text
THE MISSING CARAVAN

Search the Old Road
0 / 1
```

Tracker не должен превращаться в полноценный Quest Log.

---

## 17. Tracker capacity

Позволить tracking нескольких quests в будущем, но UX ограничить.

Рекомендуемый максимум одновременно pinned:

```text
3
```

По умолчанию после Accept новый quest можно автоматически track'ать.

---

## 18. Active Quest State

Backend state:

```text
ACTIVE
```

Frontend знает:

```text
quest id
title
current stage
active objective(s)
progress
location hint
```

---

## 19. Contextual Location actions

Главный смысл quest engine — не заставлять пользователя постоянно открывать Quest Log.

Если active quest имеет objective в текущей location:

Location Screen должен уметь показать contextual quest action.

Например:

```text
QUEST OBJECTIVE

Search the abandoned wagon

[ Investigate ]
```

или:

```text
Roadside Bandits
Quest Target

[ Fight ]
```

Это должно строиться поверх общей Location/Action системы, а не отдельным quest-specific page.

---

## 20. Progress

Quest objective должен обновляться от игровых событий.

Примеры:

```text
Enemy defeated
Item collected
Location visited
Interaction completed
Craft completed
Arena battle won
NPC talked to
```

Frontend **не должен сам решать**, выполнен ли quest.

Источник истины — backend quest progression.

---

## 21. Quest Progress Service

Нужен единый backend service/domain layer:

```text
QuestProgressService
```

Концептуально:

```text
onEnemyDefeated(...)
onItemAcquired(...)
onLocationVisited(...)
onInteractionCompleted(...)
onCraftCompleted(...)
onArenaResult(...)
```

Не раскидывать:

```text
if quest == ...
```

по combat/inventory/location services.

---

## 22. Objective Types v1

Заложить extensible enum/type system.

Минимально:

```text
KILL
COLLECT
VISIT
INTERACT
TALK
CRAFT
ARENA_WIN
```

Дополнительно можно подготовить:

```text
CUSTOM
```

но не использовать его как способ сделать все objectives custom scripts.

---

## 23. Multi-stage quests

Quest должен поддерживать stages.

Например:

```text
Stage 1
Search the Old Road

Stage 2
Investigate Wagon

Stage 3
Defeat Bandits

Stage 4
Return to Varro
```

Не обязательно создавать отдельную database entity `QuestStage`, если objectives с `order/stage` достаточно для текущей модели.

Но модель не должна предполагать:

> все objectives всегда активны одновременно.

---

## 24. Progress Feedback

При существенном обновлении:

```text
QUEST UPDATED
The Missing Caravan

Investigate the Abandoned Wagon
```

Использовать небольшой toast.

Для счетчиков:

```text
Wolf Pelts
3 / 6
```

не показывать огромный popup на каждый +1.

Tracker достаточно обновить inline.

---

## 25. Ready to Turn In

Когда gameplay objectives закончены:

```text
ACTIVE
↓
READY_TO_TURN_IN
```

Tracker становится:

```text
THE MISSING CARAVAN

✓ Objectives Complete

Return to Captain Varro
```

Если turn-in location известна, location/navigation UI может подсветить соответствующее направление.

---

## 26. Turn-in

При взаимодействии с quest giver после completion objectives открыть небольшой completion preview:

```text
THE MISSING CARAVAN
QUEST COMPLETE

Captain Varro:
...

Rewards:
320 XP
85 Silver
Supply Satchel

[ CLAIM REWARDS ]
```

Это отдельный будущий visual state.

Dedicated mockup пока отсутствует.

Перед high-fidelity implementation этого состояния желательно создать отдельный mock.

---

## 27. Completion

После claim:

```text
READY_TO_TURN_IN
↓
COMPLETED
```

Выдать rewards атомарно.

Нельзя допускать:

```text
quest completed
but rewards partially failed
```

Completion + rewards должны быть одной backend transaction.

---

## 28. Quest State Machine

Рекомендуемая модель:

```text
AVAILABLE
    ↓ accept

ACTIVE
    ↓ objectives complete

READY_TO_TURN_IN
    ↓ reward claimed

COMPLETED
```

Дополнительно:

```text
ACTIVE → ABANDONED
```

`AVAILABLE` желательно считать derived state, а не обязательно хранить отдельную PlayerQuest запись.

---

## 29. Quest Definition

Quest definition должна хранить минимум:

```text
id
code

title
shortDescription
description

questType
recommendedLevel

sourceType
sourceId

startLocationId
turnInLocationId

objectives

rewards

prerequisites
nextQuestId

repeatability
enabled
```

---

## 30. Player Quest

Отдельно от definition:

```text
playerId
questId

status

acceptedAt
completedAt

currentStage

objectiveProgress
```

Нельзя изменять shared quest definition при прогрессе конкретного игрока.

---

## 31. Quest prerequisites

Архитектура должна позволить впоследствии:

```text
minimum level
previous quest completed
location unlocked
feature unlocked
```

Для первого релиза достаточно level + previous quest.

---

## 32. Notice Board availability

Notice Board не должен получать просто:

```text
all quests
```

Backend должен вернуть только те quests, которые:

- относятся к этой доске/location;
- enabled;
- доступны по prerequisite;
- не completed, если quest non-repeatable;
- не active;
- либо active помечены соответствующим state.

---

## 33. Suggested API surface

Имена адаптировать под существующие conventions проекта.

Концептуально:

```text
GET /locations/{locationId}/quest-board
```

Возвращает quest summaries для Step 1.

```text
GET /quests/{questId}
```

Full Preview для Step 2.

```text
POST /quests/{questId}/accept
```

Accept.

```text
GET /player/quests
```

Active / tracker / Quest Log.

```text
POST /quests/{questId}/abandon
```

Future/optional.

```text
POST /quests/{questId}/turn-in
```

Complete + rewards.

Не создавать frontend API endpoint для ручного increment objective progress.

---

## 34. Frontend component boundaries

Рекомендуемые logical components:

```text
NoticeBoard
QuestList
QuestListItem

QuestPreview
QuestArtwork
QuestObjectives
QuestRewards
QuestGiver

QuestTracker
QuestTrackerItem

QuestAcceptedToast
QuestUpdatedToast
```

Не делать один:

```text
QuestPage.tsx
```

на несколько тысяч строк.

---

## 35. NoticeBoard internal state

Пример:

```text
mode:
  LIST
  PREVIEW

selectedQuestId
```

Важно:

Step 1 и Step 2 — **два состояния одного component**, а не две отдельные pages.

---

## 36. Route behavior

Для v1 не требуется менять URL при выборе quest.

Игрок остается на:

```text
Location
```

Notice Board является contextual UI.

В будущем можно поддержать deep-link через query state:

```text
?panel=notice-board&quest=...
```

но это не prerequisite первой реализации.

---

## 37. Quest Log

Global `Quests` screen можно реализовать отдельной следующей задачей.

Quest Engine не должен зависеть от его наличия.

В первой итерации достаточно:

```text
Notice Board
Preview
Accept
Tracker
Progress
Turn-in
```

---

## 38. Activity integration

Quest события должны писать lightweight events в Activity:

```text
Quest accepted:
The Missing Caravan

Quest completed:
The Missing Caravan
```

Не отправлять туда каждое изменение `3/6 → 4/6`.

---

## 39. Notification policy

Использовать Notifications только для событий, требующих внимания.

Например:

```text
Quest Ready to Turn In
```

Обычный `Quest Updated` достаточно показывать toast/tracker.

---

## 40. Error states

Обязательно обработать:

### Quest became unavailable

Например другой state изменился между preview и accept.

Показать:

```text
This quest is no longer available.
```

и refresh Board.

### Level requirement

Не отправлять пользователя в generic error page.

### Already accepted

Sync active state и показать tracker.

### Network/API failure

Notice Board остается открытым.

Не терять selected quest.

---

## 41. Loading states

Step 1:

показывать lightweight Board skeleton.

Step 2:

list остается visible, preview area показывает loading state.

Не очищать всю панель.

---

## 42. Empty Notice Board

Если квестов нет:

```text
NOTICE BOARD

No new notices have been posted.

Check back later.
```

Не показывать пустую черную панель.

---

## 43. Desktop priority

Quest workflow проектируется прежде всего под desktop.

Не превращать Preview в mobile modal на desktop.

Основной interaction:

```text
panel expansion
+
persistent list
+
persistent world context
```

---

## 44. Visual continuity — acceptance requirement

Это один из ключевых критериев всей задачи.

При переходе:

```text
Step 1 → Step 2
```

не должны менять свое положение:

```text
Top Bar
Left Navigation
Activity
Chat
правый edge Notice Board
```

Меняется только Notice Board workspace.

---

## 45. Visual references

### Quest Discovery

**смотри картинки: `quest-step-1-notice-board.png`**

Использовать как source of truth для:

- global shell;
- location + board composition;
- quest list;
- chat placement;
- Activity placement.

### Quest Preview

**смотри картинки: `quest-step-2-quest-preview.png`**

Использовать как source of truth для:

- expanded Notice Board;
- Preview слева;
- persistent quest list справа;
- artwork;
- objectives;
- rewards;
- Accept action.

### Active Tracker

**смотри картинки: `quest-step-3-active-tracker.png`**

Использовать только для:

- Quest Accepted state;
- tracker idea;
- objective progress hierarchy.

Для global shell по-прежнему:

**смотри картинки: `quest-step-1-notice-board.png`**

Combat HUD из старого Step 3 reference не переносить.

---

## 46. Suggested Cursor implementation breakdown

Не отдавать всю эту документацию Cursor одним огромным implementation prompt.

Разбить на отдельные чаты.

### Task 1 — Quest Domain Model

Реализовать:

```text
QuestDefinition
PlayerQuest
Objective definitions
Objective progress
Statuses
Rewards
Prerequisites
```

Без UI.

### Task 2 — Quest Availability + Notice Board API

Реализовать:

```text
quest availability
location/board association
quest summaries
quest details
```

Добавить тесты.

### Task 3 — Accept / Active Quest Backend

Реализовать:

```text
accept
duplicate protection
prerequisite validation
active quest retrieval
```

Добавить transactional/service tests.

### Task 4 — Quest Progress Engine

Реализовать:

```text
objective handlers
multi-stage progression
READY_TO_TURN_IN
```

Без UI.

### Task 5 — Completion / Rewards

Реализовать:

```text
turn-in
reward issuing
COMPLETED
```

Transactional tests обязательны.

### Task 6 — Notice Board UI / Step 1

**смотри картинки: `quest-step-1-notice-board.png`**

Реализовать только:

```text
open/close Board
quest list
filters
availability states
loading/error/empty
```

Не реализовывать Preview заранее.

### Task 7 — Expanded Preview / Step 2

**смотри картинки: `quest-step-2-quest-preview.png`**

Реализовать:

```text
LIST → PREVIEW transition
panel expands left
persistent list
quest details
objectives
rewards
Accept
```

Особо проверить absence of page rerender/layout jump.

### Task 8 — Tracker / Step 3

**смотри картинки: `quest-step-3-active-tracker.png`**

и для shell:

**смотри картинки: `quest-step-1-notice-board.png`**

Реализовать:

```text
Quest Accepted feedback
Quest Tracker
progress refresh
Ready to Turn In
```

Не добавлять combat HUD.

### Task 9 — Contextual Objective Integration

Связать quest engine с:

```text
Location
Combat
Inventory/loot
Craft
Arena
Interactions
```

через общий progression service.

Не хардкодить отдельные quest IDs.

### Task 10 — Integration Tests + Workflow QA

Проверить минимум:

```text
available
→ preview
→ accept
→ active
→ objective progress
→ stage transition
→ ready
→ turn-in
→ rewards
→ completed
```

И edge cases:

```text
duplicate accept
invalid prerequisite
invalid turn-in
duplicate reward
abandon
```

---

## 47. Definition of Done v1

Quest Engine v1 готов, когда игрок может:

1. находиться в Location;
2. открыть Notice Board;
3. увидеть доступные quests;
4. открыть Preview без перехода на другую page;
5. переключаться между quests;
6. принять quest;
7. вернуться к Location;
8. видеть tracker;
9. выполнять objectives;
10. видеть progress;
11. получить `Ready to Turn In`;
12. вернуться к quest giver;
13. завершить quest;
14. получить rewards ровно один раз.

При этом весь workflow должен ощущаться как **непрерывное взаимодействие внутри игрового мира**, а не как серия несвязанных CRUD-экранов.

---

## Recommended repository structure

Документ:

```text
docs/features/quests/QUEST_ENGINE_V1.md
```

Визуальные references:

```text
docs/ui/reference/quests/
    quest-step-1-notice-board.png
    quest-step-2-quest-preview.png
    quest-step-3-active-tracker.png
```

Каждый отдельный Cursor-чат должен получать:

- одну небольшую implementation task;
- ссылку на `QUEST_ENGINE_V1.md`;
- конкретный mockup только там, где он действительно нужен.
