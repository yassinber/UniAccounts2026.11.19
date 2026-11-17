# شرح مشروع UniAccounts للمناقشة

> الهدف من هذا الملف أن تفهم كل سطر في المشروع وتستطيع الدفاع عنه شفوياً.
> اقرأه ومعك الكود مفتوحاً. أسماء الملفات كلها داخل `src/dz/univ/uniaccounts/`.

---

## 1. الصورة الكبيرة في دقيقة

- **العمل الأول** يجيب عن سؤال: **كيف ننشئ؟**
  - الحساب يُبنى بـ **Builder**.
  - المستودع وسجل التدقيق نسخة واحدة لكل منهما (**Singleton**).
  - الطلبة المتشابهون يُنسخون من قالب (**Prototype**).
- **العمل الثاني** يجيب عن سؤال: **كيف ننظّم ونوسّع ونجعل الأجزاء تتفاعل؟**
  - **State**: للحساب حالات.
  - **Observer**: كل حدث يصل إلى ثلاثة مراقبين.
  - **Strategy**: التصدير بصيغتين.
  - **Composite**: الجامعة شجرة.
  - **Adapter**: النظام القديم يُستورد عبر محوّل.
  - **Decorator**: الخدمات تُضاف كطبقات.
  - **Facade**: واجهة واحدة تخفي كل ذلك.

### تتبّع طلب واحد من البداية إلى النهاية

ماذا يحدث عند تنفيذ `uni.activate("20260001")` في `Work2Demo`؟

1. **Facade**: الدالة `UniAccountsFacade.activate` تستدعي `changeState`.
2. **Singleton**: `changeState` يجلب الحساب من `AccountRepository.getInstance()`.
3. **State**: يستدعي `account.activate()`، والحساب لا يقرر شيئاً بنفسه، بل ينفّذ `state = state.activate()`:
   - إذا كانت الحالة `PendingState`، فإنها ترجع `new ActiveState()`، فتتغير الحالة.
   - إذا كانت `ClosedState`، فإنها ترمي `IllegalStateException`.
4. **عند الرفض**: الـ Facade يمسك الاستثناء، ويكتب `REFUSED` في السجل، ويرجع `false`. **لا يُرسَل أي إشعار**.
5. **Observer عند النجاح**: الـ Facade يستدعي `events.publish(ACTIVATED, account)`، والـ Subject يمر على المراقبين الثلاثة:
   - `AuditObserver` يكتب في `AuditLogger` (الـ Singleton).
   - `EmailNotifier` يطبع رسالة للطالب.
   - `AdminDashboard` يزيد العداد.

إذا فهمت هذا المسار فقد فهمت كيف تتعاون خمسة أنماط معاً.

---

## 2. كل نمط: أين هو؟ وكيف تشرحه؟

### Builder

- **الملفات:** `model/StudentAccountBuilder.java` (Builder) و`model/StudentAccount.java` (Product).
- **الفكرة:**
  - الحقول الإجبارية الثلاثة وسائط لمُنشئ الـ Builder، فلا يمكن نسيانها.
  - الحقول الاختيارية دوال بأسمائها، وكل دالة ترجع `this` لنستطيع السَّلسلة: `.level("M1").major("Info")`.
  - `build()` يتحقق من القواعد ثم ينشئ الحساب.
- **لماذا مُنشئ `StudentAccount` ليس `public`؟** حتى لا يستطيع أحد إنشاء حساب دون المرور بالتحقق. والـ Builder في الحزمة نفسها، فيستطيع استدعاءه.
- **أين القيم الافتراضية؟** في حقول الـ Builder: `level = "L1"`، و`scholarship = false`، و`courses` قائمة فارغة، و`phone = null`.
- **لماذا لا يتحقق الـ Builder من تفرّد المعرّف؟** لأنه لا يعرف الحسابات الموجودة. المستودع وحده يعرفها، فهو يتحقق.

### Singleton

- **الملفات:** `repository/AccountRepository.java` و`audit/AuditLogger.java`.
- **الأجزاء الثلاثة:**
  1. مُنشئ `private`.
  2. صنف داخلي `Holder` يحمل النسخة الوحيدة في حقل `static final`.
  3. `getInstance()` ترجعها.
- **لماذا Holder؟**
  - **كسول (lazy):** الـ JVM لا تحمّل `Holder` إلا عند أول استدعاء لـ `getInstance()`.
  - **آمن مع الخيوط (thread-safe):** الـ JVM تضمن أن تهيئة الصنف تحدث مرة واحدة فقط، فلا نحتاج `synchronized` عند الإنشاء.
- **البدائل:**
  - الإنشاء المبكر (`static final INSTANCE = new ...` مباشرة).
  - `synchronized getInstance()`، وهو أبطأ.
  - `enum` بعنصر واحد، ويحمي أيضاً من الانعكاس (reflection) والتسلسل (serialization).
- **لماذا توجد `clear()`؟** للاختبارات فقط. الـ Singleton يعيش طوال تشغيل البرنامج، فنفرغه في بداية كل مجموعة اختبارات.

### Prototype

- **الملفات:**
  - `model/AccountPrototype.java`: الواجهة.
  - `StudentAccount`: ينفّذ `copy()` و`copyWithIdentity()`.
  - `template/AccountTemplateRegistry.java`: سجل القوالب.
- **أهم نقطة: النسخ العميق.** في مُنشئ النسخ نكتب:

  ```java
  this.courses = new ArrayList<String>(source.courses);
  ```

  - بهذا تحصل كل نسخة على قائمة **جديدة**.
  - لو كتبنا `this.courses = source.courses` لتشارك الجميع القائمة نفسها (نسخ سطحي).
  - برهان ذلك في الصورة `work1-03-prototype.png`: أضفنا «Anglais» لـ Laila ولم يتغير غيرها.
- **لماذا لا نستعمل `clone()`؟**
  - `Object.clone()` سطحي افتراضياً.
  - يتطلب واجهة `Cloneable` والتعامل مع `CloneNotSupportedException`.
  - مُنشئ النسخ أوضح.
- **لماذا يحفظ السجل نسخة من القالب؟** لكي لا يتغير القالب المسجّل إذا عدّل المستدعي الكائن الذي مرره لاحقاً. وهذا مُختبَر.
- **لماذا لا يُحفظ القالب في المستودع؟** لأنه نموذج، لا طالب حقيقي.

### State

- **الملفات:** `state/AccountState.java` (الواجهة) والأصناف الأربعة. و`StudentAccount` هو الـ Context.
- **الفكرة:** كل دالة في الحالة ترجع **الحالة التالية** أو ترمي استثناء. والحساب يكتب فقط `state = state.activate();`.
- **جدول الانتقالات:** في المخطط `docs/uml/work2-state-diagram.png`.
  - الدخول مسموح في `ACTIVE` فقط.
  - `CLOSED` حالة نهائية.
- **ما الذي نصّ عليه التكليف وما الذي اخترناه؟**
  - **من التكليف:** الحالات الأربع، والحساب الجديد لا يُستعمل مباشرة، ولا دخول للمعلّق.
  - **من اختيارنا:** إعادة التفعيل بعد التعليق، والغلق من `PENDING` (انسحاب).
- **الفرق بين `AccountStatus` و`AccountState`:**
  - `AccountStatus` مجرد `enum` للاسم، يُستعمل في العرض والتصدير.
  - `AccountState` هو **السلوك**.
- **لماذا يبدأ الحساب المنسوخ من قالب في `PENDING`؟** لأن الحقل مهيّأ عند التصريح به: `private AccountState state = new PendingState();`، والـ JVM تنفّذ هذا السطر في كل مُنشئ، بما فيه مُنشئ النسخ.

### Observer

- **الملفات:** كلها في `observer/`.
  - `AccountEventPublisher`: الـ Subject.
  - `AccountObserver`: الواجهة.
  - ثلاثة مراقبين: `AuditObserver` و`EmailNotifier` و`AdminDashboard`.
  - `AccountEvent` و`EventType`: بيانات الحدث.
- **من يطلق الأحداث؟** الـ Facade:
  - `CREATED` عند التسجيل.
  - `ACTIVATED` و`SUSPENDED` و`CLOSED` بعد نجاح الانتقال.
  - `IMPORTED` عند الاستيراد.
- **لماذا ليس الحساب هو الـ Subject؟**
  - حتى لا يحمل كل حساب قائمة مراقبين.
  - ولأن الإنشاء والاستيراد يقعان خارج الحساب.
- **`AuditObserver` يعيد استعمال الـ Singleton** `AuditLogger` ولا ينشئ سجلاً خاصاً به.
- **كيف أضيف مراقباً رابعاً (مثلاً SMS)؟** أكتب صنفاً ينفّذ `AccountObserver`، ثم أضيف سطراً واحداً: `events.attach(new SmsNotifier())`. ولا يتغير أي كود آخر.

### Strategy

- **الملفات:** `export/`.
  - `ExportStrategy`: الواجهة.
  - `CsvExport` و`JsonExport`: الاستراتيجيتان.
  - `AccountExporter`: الـ Context.
- **CSV:**
  - سطر العناوين `id,name,level,status` حرفياً كما في التكليف.
  - إذا احتوت قيمة على فاصلة توضع بين علامتي تنصيص، وهي قاعدة CSV المعروفة.
- **JSON:** مكتوب يدوياً بلا مكتبة، مع تهريب `"` و`\`.
- **الفرق بين Strategy وState** (سؤال كلاسيكي):
  - البنية متشابهة.
  - في **Strategy** **العميل** هو من يختار الخوارزمية (`setStrategy`)، والاستراتيجيات لا تعرف بعضها.
  - في **State** **الحالة نفسها** تقرر الحالة التالية، والعميل لا يختار.

### Adapter

- **الملفات:**
  - `adapter/StudentSource.java`: الـ Target.
  - `adapter/LegacyStudentAdapter.java`: الـ Adapter.
  - `legacy/LegacyStudentSystem.java`: الـ Adaptee.
- **التحويل:** يقسّم السطر على `;`، ثم:
  - `fullName = FIRSTNAME + " " + LASTNAME`.
  - البناء **بالـ Builder**، فتمر البيانات القديمة بالتحقق نفسه.
- **Object Adapter مقابل Class Adapter:**
  - نحن نستعمل **التركيب** (الحقل `legacy`) لا الوراثة.
  - هكذا نعتمد على الدالة العامة `fetchRecords()` فقط.
  - ويمكن تغيير المصدر دون تغيير الـ Adapter.
- **السطر المعيب:** لا يوقف الاستيراد، بل يذهب إلى `getRejectedRecords()`. ومُختبَر بنظام قديم مزيّف فيه أسطر خاطئة.
- **المعرّف المكرر `19990008`:**
  - الـ Adapter **لا** يكتشفه، لأنه لا يعرف المستودع.
  - المستودع يرفض الثاني (`DuplicateAccountException`).
  - الـ Facade يكمل الاستيراد ويضع المرفوض في `ImportReport`.
  - **النتيجة: 9 مستوردة و1 مرفوضة.**

### Composite

- **الملفات:** `structure/`.
  - `UniversityComponent`: المكوّن المجرد.
  - `CompositeUnit`: العقدة.
  - `University` و`Faculty` و`Department` و`StudentGroup`: العقد الملموسة.
  - `AccountLeaf`: الورقة.
- **السطر الأهم** في `CompositeUnit.countAccounts()`:

  ```java
  total += child.countAccounts();
  ```

  - العقدة لا تعرف هل ابنها ورقة أم شجرة كاملة.
  - هذا استدعاء ذاتي (recursion) ينتهي عند الأوراق التي ترجع 1.
- **لماذا `AccountLeaf` يغلّف الحساب بدل أن يرث `StudentAccount` من `UniversityComponent`؟** حتى لا نعدّل صنف العمل الأول مرة ثانية، وحتى لا نخلط مسؤولية الحساب بمسؤولية الشجرة.
- **الشفافية مقابل الأمان (transparency vs safety):**
  - `add()` موجودة في `CompositeUnit` فقط، لا في `UniversityComponent`.
  - لذلك لا يمكن استدعاء `add` على ورقة، وهذا هو الخيار «الآمن».

### Decorator

- **الملفات:** `services/`.
- **المزخرف يحقق علاقتين معاً:**
  - **هو** `StudentServices` (`implements`)، فيمكن وضعه مكان أي `StudentServices`.
  - **يحمل** `StudentServices` (الحقل `wrapped`) ويفوّض له.
- **مثال:** `new InternetService(new LibraryService(new BasicServices()))`:
  - `getDescription()` تبني «Basic account + Library + Internet» من الداخل إلى الخارج.
  - `canAccess("LIBRARY")`: طبقة الإنترنت لا تعرف المكتبة، فتسأل الطبقة التي تحتها، وهذه تجيب `true`.
- **لماذا لا نستعمل الوراثة؟** ثلاث خدمات تعطي 2³ = 8 أصناف لكل التوليفات، وأربع تعطي 16. ومع Decorator يكفي صنف واحد لكل خدمة.
- **الفرق بين Decorator وComposite** (سؤال محتمل):
  - كلاهما يحمل كائناً من النوع نفسه.
  - Decorator يحمل **واحداً** و**يضيف سلوكاً**.
  - Composite يحمل **عدة أبناء** و**يجمع نتائجهم**.

### Facade

- **الملف:** `facade/UniAccountsFacade.java`.
- **الفكرة:** دالة لكل حالة استعمال. والعميل `Work2Demo` لا يعرف إلا الـ Facade.
- **الـ Facade ينسّق ولا يكرر المنطق.** مثلاً، التحقق من صحة الانتقال يبقى في الحالات، والـ Facade يرتّب فقط: «جلب، ثم انتقال، ثم إشعار».
- **ما الفرق بينه وبين Adapter؟**
  - **Adapter** يحوّل واجهة **موجودة** إلى واجهة **متوقعة** (واجهة مقابل واجهة).
  - **Facade** يبسّط **نظاماً كاملاً** بواجهة جديدة.
- **هل الـ Facade يمنع استعمال الأصناف مباشرة؟** لا. هو يقدّم طريقاً بسيطاً، والأصناف تبقى متاحة لمن يحتاجها (مثلاً الاختبارات تستعملها مباشرة).

---

## 3. العلاقات في مخطط UML: كيف تقرأها

| الرمز | المعنى | مثال في مشروعنا |
|---|---|---|
| خط متصل برأس مثلث فارغ | وراثة (`extends`) | `Faculty` ← `CompositeUnit` |
| خط متقطع برأس مثلث فارغ | تنفيذ واجهة (`implements`) | `ActiveState` ← `AccountState` |
| معيّن فارغ | تجميع (aggregation): «يحتوي على» والجزء يعيش وحده | `AccountRepository` ◇ `StudentAccount` |
| سهم متصل | ارتباط (association): حقل يشير إلى كائن | `StudentAccount` → `AccountState` |
| سهم متقطع | اعتماد (dependency): استعمال مؤقت | `LegacyStudentAdapter` ⇢ `StudentAccountBuilder` |

---

## 4. أسئلة متوقعة في المناقشة، مع إجابات قصيرة

1. **لماذا اخترت Holder للـ Singleton؟** لأنه كسول وآمن مع الخيوط دون `synchronized`، والـ JVM تضمن تهيئة الصنف الداخلي مرة واحدة.
2. **هل يمكن كسر الـ Singleton؟** نعم، بالانعكاس (`setAccessible`) أو بالتسلسل (serialization). والـ `enum` يحمي من الاثنين. وصنفانا لا ينفّذان `Serializable`.
3. **ما الفرق بين النسخ السطحي والعميق؟** السطحي ينسخ المراجع، فتتشارك النسختان القائمة نفسها. والعميق ينشئ قائمة جديدة. ونحن ننسخ عميقاً لقائمة المقررات. أما السلاسل النصية فثابتة (immutable)، فلا تحتاج نسخاً.
4. **أين تتحقق من صحة البريد؟** في `AccountRules.checkEmail`، ويستدعيها الـ Builder عند `build()` والـ Prototype عند تغيير الهوية. القاعدة الوحيدة المطلوبة هي وجود `@`.
5. **لماذا قُبل البريد `y. Rida@old.univ.dz` وفيه مسافة؟** لأنه يحقق القاعدة المطلوبة (يحتوي `@`). ولم نضف قواعد أشد من المطلوب. ويمكن تشديدها في `AccountRules` إن طلب الأستاذ.
6. **ماذا فعلت بالـ `19990008` المكرر؟** المستودع رفض الثاني، والاستيراد أكمل، والتقرير يبيّن السبب. أبقينا الأول (Hoda triqui) لأنه وصل أولاً.
7. **هل عدّلت `LegacyStudentSystem`؟** الأصل لا يُترجَم (`");` بدل `",`)، وبلا حزمة. حفظناه حرفياً في `docs/legacy-original`، وفي النسخة المترجمة صحّحنا الترقيم وأضفنا الحزمة **فقط**. البيانات والدالة كما هي، وكل التحويل في الـ Adapter. والأمر `diff` يثبت ذلك.
8. **ماذا عدّلت في العمل الأول؟** أضفت الحالة إلى `StudentAccount` فقط (نمط State يتطلب أن يحمل الـ Context حالته)، وسطر `serialVersionUID`. الباقي لم يتغير، و`git diff work1 work2` يثبت ذلك.
9. **لماذا لا يصل إشعار عند رفض التعليق؟** لأن الـ Facade ينشر الحدث بعد نجاح الانتقال فقط. الرفض يُسجَّل في السجل بالوسم `REFUSED`.
10. **ما الفرق بين State وStrategy؟** راجع قسم Strategy أعلاه: في State الحالة تقرر الحالة التالية، وفي Strategy العميل يختار.
11. **كيف أضيف صيغة تصدير XML؟** صنف `XmlExport implements ExportStrategy`، وسطر في مُنشئ الـ Facade لتسجيله. ولا يتغير `CsvExport` ولا `JsonExport`.
12. **كيف أضيف خدمة «نقل جامعي»؟** صنف `TransportService extends ServiceDecorator`، وفرع في `subscribe`.
13. **كيف أضيف حالة «GRADUATED»؟** صنف جديد ينفّذ `AccountState`، وقيمة في `AccountStatus`، ثم تحديد الحالات التي تنتقل إليه. ولا يتغير `StudentAccount`.
14. **لماذا Object Adapter وليس Class Adapter؟** في Java لا يوجد إلا وراثة واحدة. والتركيب لا يربطنا بتفاصيل الصنف القديم، ويسمح بتبديل المصدر (في الاختبارات نمرر نظاماً مزيّفاً).
15. **هل الـ Facade Singleton؟** لا، ولا حاجة لذلك. المستودع والسجل هما الـ Singleton-ان، فكل نسخ الـ Facade تشترك فيهما.
16. **لماذا الـ Builder في ملف منفصل؟** التكليف يطلب أن يكون كل صنف في ملف منفصل. والطريقة الشائعة (صنف متداخل ساكن) صحيحة أيضاً.
17. **كيف تختبر بدون JUnit؟** `test/Check.java` مساعد صغير لدوال التحقق، و`AllTests` يشغّل 140 فحصاً. اخترنا ذلك لكي لا يحتاج المشروع أي تحميل.
18. **ما المبادئ التي تحترمها الأنماط؟**
    - **Open/Closed:** نضيف صيغة أو حالة أو خدمة أو مراقباً دون تعديل الموجود.
    - **Single Responsibility:** كل صنف مسؤولية واحدة.
    - **الاعتماد على الواجهات لا على التنفيذ:** `ExportStrategy` و`AccountObserver` و`StudentSource`.

---

## 5. قبل المناقشة: تمرين مقترح

1. شغّل `./run.sh` واقرأ المخرجات قسماً قسماً مع الكود.
2. احذف السطر `this.courses = new ArrayList<String>(source.courses);`، وضع مكانه `this.courses = source.courses;`. شغّل `./run.sh test` وشاهد أي اختبارات تفشل، ثم أرجعه. هذا يجعلك تفهم النسخ العميق فعلاً.
3. أضف مراقباً رابعاً يطبع سطراً، وأضفه بـ `attach` في الـ Facade، ولاحظ أنك لم تلمس أي كود آخر.
4. ارسم مخطط الحالات من الذاكرة.
