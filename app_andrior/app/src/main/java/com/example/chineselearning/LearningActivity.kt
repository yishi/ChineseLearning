package com.example.chineselearning

import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

data class CharacterData(
    val character: String,
    val pinyin: String,
    val meaning: String,
    val example: String,
    val level: Int // 难度级别
)

class LearningActivity : AppCompatActivity() {
    private lateinit var characterTextView: TextView
    private lateinit var pinyinTextView: TextView
    private lateinit var meaningTextView: TextView
    private lateinit var exampleTextView: TextView
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button

    private var currentIndex = 0
    private val characters = listOf(
        // 第一级汉字（1-100）
        CharacterData("我", "wǒ", "I, me", "我是学生。(I am a student.)", 1),
        CharacterData("你", "nǐ", "you", "你好！(Hello!)", 1),
        CharacterData("他", "tā", "he", "他是老师。(He is a teacher.)", 1),
        CharacterData("她", "tā", "she", "她是医生。(She is a doctor.)", 1),
        CharacterData("们", "men", "plural marker", "我们是朋友。(We are friends.)", 1),
        CharacterData("这", "zhè", "this", "这是我的书。(This is my book.)", 1),
        CharacterData("那", "nà", "that", "那是他的家。(That is his home.)", 1),
        CharacterData("是", "shì", "to be", "他是中国人。(He is Chinese.)", 1),
        CharacterData("有", "yǒu", "to have", "我有一本书。(I have a book.)", 1),
        CharacterData("爱", "ài", "to love", "我爱我的家人。(I love my family.)", 1),
        CharacterData("想", "xiǎng", "to want; to think", "我想学中文。(I want to learn Chinese.)", 1),
        CharacterData("学", "xué", "to study", "我学习汉字。(I study Chinese characters.)", 1),
        CharacterData("说", "shuō", "to speak", "我说中文。(I speak Chinese.)", 1),
        CharacterData("看", "kàn", "to look; to watch", "我在看书。(I am reading a book.)", 1),
        CharacterData("听", "tīng", "to listen", "我喜欢听音乐。(I like listening to music.)", 1),
        CharacterData("走", "zǒu", "to walk", "我们走吧。(Let's go.)", 1),
        CharacterData("来", "lái", "to come", "请过来。(Please come over.)", 1),
        CharacterData("去", "qù", "to go", "我要去学校。(I am going to school.)", 1),
        CharacterData("好", "hǎo", "good", "今天天气很好。(The weather is good today.)", 1),
        CharacterData("大", "dà", "big", "这是一个大房子。(This is a big house.)", 1),
        CharacterData("小", "xiǎo", "small", "一只小猫。(A small cat.)", 1),
        CharacterData("多", "duō", "many, much", "这里有很多人。(There are many people here.)", 1),
        CharacterData("少", "shǎo", "few, little", "我的错误很少。(I make few mistakes.)", 1),
        CharacterData("快", "kuài", "fast", "他跑得很快。(He runs very fast.)", 1),
        CharacterData("慢", "màn", "slow", "请说慢一点。(Please speak more slowly.)", 1),
        CharacterData("新", "xīn", "new", "这是一件新衣服。(This is a new clothes.)", 1),
        CharacterData("旧", "jiù", "old", "这是一本旧书。(This is an old book.)", 1),
        CharacterData("高", "gāo", "tall", "他很高。(He is tall.)", 1),
        CharacterData("矮", "ǎi", "short", "她很矮。(She is short.)", 1),
        CharacterData("爸", "bà", "father", "爸爸在工作。(Father is working.)", 1),
        CharacterData("妈", "mā", "mother", "妈妈在做饭。(Mother is cooking.)", 1),
        CharacterData("哥", "gē", "elder brother", "我有一个哥哥。(I have an elder brother.)", 1),
        CharacterData("姐", "jiě", "elder sister", "这是我的姐姐。(This is my elder sister.)", 1),
        CharacterData("弟", "dì", "younger brother", "弟弟在玩游戏。(Younger brother is playing games.)", 1),
        CharacterData("妹", "mèi", "younger sister", "我有一个妹妹。(I have a younger sister.)", 1),
        CharacterData("家", "jiā", "family; home", "我爱我的家。(I love my family.)", 1),
        CharacterData("爷", "yé", "grandfather", "爷爷在看电视。(Grandfather is watching TV.)", 1),
        CharacterData("奶", "nǎi", "grandmother", "奶奶在花园。(Grandmother is in the garden.)", 1),
        CharacterData("一", "yī", "one", "一个苹果。(One apple.)", 1),
        CharacterData("二", "èr", "two", "两个人。(Two people.)", 1),
        CharacterData("三", "sān", "three", "三本书。(Three books.)", 1),
        CharacterData("四", "sì", "four", "四个季节。(Four seasons.)", 1),
        CharacterData("五", "wǔ", "five", "五个手指。(Five fingers.)", 1),
        CharacterData("六", "liù", "six", "六天。(Six days.)", 1),
        CharacterData("七", "qī", "seven", "七个月。(Seven months.)", 1),
        CharacterData("八", "bā", "eight", "八个小时。(Eight hours.)", 1),
        CharacterData("九", "jiǔ", "nine", "九个学生。(Nine students.)", 1),
        CharacterData("十", "shí", "ten", "十年。(Ten years.)", 1),
        CharacterData("百", "bǎi", "hundred", "一百块钱。(One hundred yuan.)", 1),
        CharacterData("千", "qiān", "thousand", "一千公里。(One thousand kilometers.)", 1),
        CharacterData("书", "shū", "book", "这是一本书。(This is a book.)", 1),
        CharacterData("人", "rén", "person", "他是好人。(He is a good person.)", 1),
        CharacterData("水", "shuǐ", "water", "我想喝水。(I want to drink water.)", 1),
        CharacterData("火", "huǒ", "fire", "火很热。(Fire is hot.)", 1),
        CharacterData("山", "shān", "mountain", "这座山很高。(This mountain is high.)", 1),
        CharacterData("口", "kǒu", "mouth", "张开口。(Open your mouth.)", 1),
        CharacterData("手", "shǒu", "hand", "洗手。(Wash your hands.)", 1),
        CharacterData("心", "xīn", "heart", "我的心。(My heart.)", 1),
        CharacterData("门", "mén", "door", "关门。(Close the door.)", 1),
        CharacterData("眼", "yǎn", "eye", "大眼睛。(Big eyes.)", 1),
        CharacterData("耳", "ěr", "ear", "耳朵很灵。(The ears are sharp.)", 1),
        CharacterData("鼻", "bí", "nose", "鼻子很高。(The nose is high.)", 1),
        CharacterData("天", "tiān", "day", "今天很忙。(Today is busy.)", 1),
        CharacterData("年", "nián", "year", "新年快乐！(Happy New Year!)", 1),
        CharacterData("月", "yuè", "month; moon", "月亮很圆。(The moon is full.)", 1),
        CharacterData("日", "rì", "sun; day", "日出很美。(The sunrise is beautiful.)", 1),
        CharacterData("时", "shí", "time", "现在是下午三时。(It's 3 o'clock in the afternoon.)", 1),
        CharacterData("分", "fēn", "minute", "五分钟。(Five minutes.)", 1),
        CharacterData("早", "zǎo", "early; morning", "早上好！(Good morning!)", 1),
        CharacterData("晚", "wǎn", "late; evening", "晚安。(Good night.)", 1),
        CharacterData("昨", "zuó", "yesterday", "昨天很冷。(Yesterday was cold.)", 1),
        CharacterData("今", "jīn", "today", "今天很热。(Today is hot.)", 1),
        CharacterData("明", "míng", "tomorrow", "明天见。(See you tomorrow.)", 1),
        CharacterData("上", "shàng", "up; above", "在上面。(Above.)", 1),
        CharacterData("下", "xià", "down; below", "在下面。(Below.)", 1),
        CharacterData("左", "zuǒ", "left", "在左边。(On the left.)", 1),
        CharacterData("右", "yòu", "right", "在右边。(On the right.)", 1),
        CharacterData("前", "qián", "front", "在前面。(In front.)", 1),
        CharacterData("后", "hòu", "back", "在后面。(Behind.)", 1),
        CharacterData("里", "lǐ", "inside", "在里面。(Inside.)", 1),
        CharacterData("外", "wài", "outside", "在外面。(Outside.)", 1),
        CharacterData("中", "zhōng", "middle", "在中间。(In the middle.)", 1),
        CharacterData("旁", "páng", "side", "在旁边。(Beside.)", 1),
        CharacterData("万", "wàn", "ten thousand", "一万个人。(Ten thousand people.)", 1),
        CharacterData("刀", "dāo", "knife", "这是一把刀。(This is a knife.)", 1),
        CharacterData("入", "rù", "to enter", "请入座。(Please be seated.)", 1),
        CharacterData("爬", "pá", "to crawl", "小猫在爬树。(The kitten is climbing the tree.)", 1),
        CharacterData("金", "jīn", "gold; metal", "金子很贵重。(Gold is precious.)", 1),
        CharacterData("木", "mù", "wood; tree", "这是木头。(This is wood.)", 1),
        CharacterData("土", "tǔ", "earth; soil", "土地很肥沃。(The soil is fertile.)", 1),
        CharacterData("目", "mù", "eye", "目不转睛。(Eyes fixed intently.)", 1),
        CharacterData("母", "mǔ", "mother", "母亲很温柔。(Mother is gentle.)", 1),
        CharacterData("公", "gōng", "public", "这是公园。(This is a public park.)", 1),
        CharacterData("田", "tián", "field", "稻田很美。(The rice field is beautiful.)", 1),
        CharacterData("电", "diàn", "electricity", "没有电了。(There's no electricity.)", 1),
        CharacterData("点", "diǎn", "point; dot", "现在几点了？(What time is it now?)", 1),
        CharacterData("禾", "hé", "grain", "禾苗长得很好。(The grain is growing well.)", 1),
        CharacterData("冲", "chōng", "to rush", "他冲向前。(He rushed forward.)", 1),
        CharacterData("虫", "chóng", "insect", "一只小虫子。(A small insect.)", 1),
        CharacterData("了", "le", "completed action marker", "吃了饭。(Have eaten.)", 1),
        CharacterData("子", "zǐ", "child; seed", "孩子很可爱。(The child is cute.)", 1),
        CharacterData("儿", "ér", "son; child", "儿子在学校。(The son is at school.)", 1),
        CharacterData("可", "kě", "can; able to", "这可以吃。(This can be eaten.)", 1),
        CharacterData("女", "nǚ", "female", "女孩很漂亮。(The girl is beautiful.)", 1),
        CharacterData("男", "nán", "male", "男孩很高。(The boy is tall.)", 1),
        CharacterData("开", "kāi", "to open", "请开门。(Please open the door.)", 1),
        CharacterData("不", "bù", "no; not", "我不去。(I'm not going.)", 1),
        CharacterData("要", "yào", "want; need", "我要这个。(I want this.)", 1),
        CharacterData("果", "guǒ", "fruit", "水果很甜。(The fruit is sweet.)", 1),
        CharacterData("乌", "wū", "black; crow", "乌云密布。(Dark clouds gather.)", 1),
        CharacterData("尺", "chǐ", "ruler", "一把尺子。(A ruler.)", 1),
        CharacterData("本", "běn", "book; root", "这本书很好。(This book is good.)", 1),
        CharacterData("林", "lín", "forest", "这是一片森林。(This is a forest.)", 1),
        CharacterData("力", "lì", "power; force", "他很有力气。(He is very strong.)", 1),
        CharacterData("王", "wáng", "king", "他姓王。(His surname is Wang.)", 1),
        CharacterData("令", "lìng", "command; order", "这是命令。(This is an order.)", 1),
        CharacterData("李", "lǐ", "plum; surname Li", "李老师很好。(Teacher Li is nice.)", 1),
        CharacterData("张", "zhāng", "surname Zhang", "我姓张。(My surname is Zhang.)", 1),
        CharacterData("立", "lì", "to stand", "他站立不动。(He stands still.)", 1),
        CharacterData("正", "zhèng", "correct; right", "这是正确的。(This is correct.)", 1),
        CharacterData("在", "zài", "at; in", "我在家。(I am at home.)", 1),
        CharacterData("比", "bǐ", "to compare", "他比我高。(He is taller than me.)", 1),
        CharacterData("石", "shí", "stone", "一块石头。(A stone.)", 1),
        CharacterData("广", "guǎng", "wide", "广场很大。(The square is big.)", 1),
        CharacterData("厂", "chǎng", "factory", "这是工厂。(This is a factory.)", 1),
        CharacterData("工", "gōng", "work", "他在工作。(He is working.)", 1),
        CharacterData("具", "jù", "tool", "工具很多。(There are many tools.)", 1),
        CharacterData("真", "zhēn", "real; true", "这是真的。(This is real.)", 1),
        CharacterData("身", "shēn", "body", "全身都疼。(The whole body hurts.)", 1),
        CharacterData("体", "tǐ", "body; form", "身体很好。(Someone's body is healthy.)", 1),
        CharacterData("包", "bāo", "bag", "书包很重。(The schoolbag is heavy.)", 1),
        CharacterData("饱", "bǎo", "full (after eating)", "我吃饱了。(I am full.)", 1),
        CharacterData("巴", "bā", "chin; tail", "她的下巴很尖。(Her chin is pointed.) / 猫的尾巴很长。(The cat's tail is long.)", 1),
        CharacterData("把", "bǎ", "handle; to hold", "把书拿来。(Bring the book.)", 1),
        CharacterData("个", "gè", "individual; measure word", "一个人。(One person.)", 1),
        CharacterData("只", "zhī", "only; measure word", "一只猫。(One cat.)", 1),
        CharacterData("题", "tí", "topic; question", "这个题很难。(This question is difficult.)", 1),
        CharacterData("半", "bàn", "half", "半个苹果。(Half an apple.)", 1),
        CharacterData("从", "cóng", "from", "从家到学校。(From home to school.)", 1),
        CharacterData("才", "cái", "just now; talent", "他很有才。(He is talented.)", 1),
        CharacterData("同", "tóng", "same", "我们是同学。(We are classmates.)", 1),
        CharacterData("自", "zì", "self", "自己的书。(One's own book.)", 1),
        CharacterData("己", "jǐ", "self", "自己。(Oneself.)", 1),
        CharacterData("的", "de", "possessive particle", "我的书。(My book.)", 1),
        CharacterData("又", "yòu", "again", "又下雨了。(It's raining again.)", 1),
        CharacterData("和", "hé", "and", "你和我。(You and me.)", 1),
        CharacterData("竹", "zhú", "bamboo", "竹子很高。(The bamboo is tall.)", 1),
        CharacterData("牙", "yá", "tooth", "刷牙很重要。(Brushing teeth is important.)", 1),
        CharacterData("用", "yòng", "to use", "用笔写字。(Write with a pen.)", 1),
        CharacterData("几", "jǐ", "how many", "几个人？(How many people?)", 1),
        CharacterData("出", "chū", "to go out", "出门了。(Going out.)", 1),
        CharacterData("见", "jiàn", "to see", "见到你很高兴。(Happy to see you.)", 1),
        CharacterData("全", "quán", "all; whole", "全部都好。(All is good.)", 1),
        CharacterData("回", "huí", "to return", "回家了。(Going back home.)", 1),

        // 第二级汉字（101-200）
        CharacterData("饭", "fàn", "meal, food", "我们去吃饭吧。(Let's go eat.)", 2),
        CharacterData("菜", "cài", "dish, vegetable", "这个菜很好吃。(This dish is delicious.)", 2),
        CharacterData("米", "mǐ", "rice", "我喜欢吃米饭。(I like to eat rice.)", 2),
        CharacterData("茶", "chá", "tea", "请喝茶。(Please drink tea.)", 2),
        CharacterData("井", "jǐng", "well", "井水很清。(The well water is clear.)", 2),

// 交通工具
        CharacterData("骑", "qí", "to ride", "我骑自行车去学校。(I ride a bicycle to school.)", 2),
        CharacterData("船", "chuán", "boat", "大船在海上。(The big ship is on the sea.)", 2),
        CharacterData("飞", "fēi", "to fly", "鸟儿在飞。(The bird is flying.)", 2),
        CharacterData("机", "jī", "machine", "飞机在天上。(The plane is in the sky.)", 2),
        CharacterData("路", "lù", "road", "这条路很长。(This road is very long.)", 2),

// 地点
        CharacterData("灯", "dēng", "lamp", "灯很亮。(The lamp is bright.)", 2),
        CharacterData("校", "xiào", "school", "学校很大。(The school is big.)", 2),
        CharacterData("店", "diàn", "shop", "商店在那里。(The shop is over there.)", 2),
        CharacterData("园", "yuán", "garden", "公园很美。(The park is beautiful.)", 2),
        CharacterData("市", "shì", "city", "这是大城市。(This is a big city.)", 2),

// 自然
        CharacterData("风", "fēng", "wind", "今天有大风。(It's windy today.)", 2),
        CharacterData("雨", "yǔ", "rain", "下雨了。(It's raining.)", 2),
        CharacterData("云", "yún", "cloud", "白云在天上。(White clouds are in the sky.)", 2),
        CharacterData("雪", "xuě", "snow", "下雪了。(It's snowing.)", 2),
        CharacterData("花", "huā", "flower", "花很漂亮。(The flowers are beautiful.)", 2),

// 动作
        CharacterData("跳", "tiào", "to jump", "小孩在跳。(The child is jumping.)", 2),
        CharacterData("跑", "pǎo", "to run", "他在跑步。(He is running.)", 2),
        CharacterData("唱", "chàng", "to sing", "她在唱歌。(She is singing.)", 2),
        CharacterData("笑", "xiào", "to laugh", "大家都在笑。(Everyone is laughing.)", 2),

// 学习用品
        CharacterData("笔", "bǐ", "pen", "这是我的笔。(This is my pen.)", 2),
        CharacterData("纸", "zhǐ", "paper", "白纸在桌上。(The white paper is on the table.)", 2),
        CharacterData("册", "cè", "volume", "这本册子很有趣。(This booklet is interesting.)", 2),
        CharacterData("桌", "zhuō", "table", "书在桌上。(The book is on the table.)", 2),
        CharacterData("椅", "yǐ", "chair", "请坐椅子。(Please sit on the chair.)", 2),

// 颜色
        CharacterData("红", "hóng", "red", "红色的花。(Red flowers.)", 2),
        CharacterData("黄", "huáng", "yellow", "黄色的星星。(Yellow stars.)", 2),
        CharacterData("蓝", "lán", "blue", "蓝色的天。(Blue sky.)", 2),
        CharacterData("白", "bái", "white", "白色的云。(White clouds.)", 2),
        CharacterData("黑", "hēi", "black", "黑色的夜。(Black night.)", 2),

// 动物
        CharacterData("鸟", "niǎo", "bird", "小鸟在飞。(The bird is flying.)", 2),
        CharacterData("猫", "māo", "cat", "猫在睡觉。(The cat is sleeping.)", 2),
        CharacterData("狗", "gǒu", "dog", "狗在跑。(The dog is running.)", 2),
        CharacterData("鱼", "yú", "fish", "鱼在游泳。(The fish is swimming.)", 2),
        CharacterData("马", "mǎ", "horse", "马在吃草。(The horse is eating grass.)", 2),

// 情感
        CharacterData("袜", "wà", "socks", "袜子很暖和。(The socks are warm.)", 2),
        CharacterData("喜", "xǐ", "happy", "我很喜欢。(I really like it.)", 2),
        CharacterData("乐", "lè", "joy", "快乐的日子。(Happy days.)", 2),
        CharacterData("怕", "pà", "afraid", "不要怕。(Don't be afraid.)", 2),
        CharacterData("哭", "kū", "cry", "别哭了。(Don't cry.)", 2),
        // 第三级汉字（201-300）
// 身体部位
        CharacterData("头", "tóu", "head", "头很疼。(My head hurts.)", 3),
        CharacterData("脸", "liǎn", "face", "她的脸很红。(Her face is red.)", 3),
        CharacterData("腿", "tuǐ", "leg", "我的腿很酸。(My legs are sore.)", 3),
        CharacterData("脚", "jiǎo", "foot", "脚很累。(My feet are tired.)", 3),
        CharacterData("背", "bèi", "back", "背很痛。(My back hurts.)", 3),

// 天气与季节
        CharacterData("春", "chūn", "spring", "春天来了。(Spring has come.)", 3),
        CharacterData("夏", "xià", "summer", "夏天很热。(Summer is hot.)", 3),
        CharacterData("秋", "qiū", "autumn", "秋天很凉爽。(Autumn is cool.)", 3),
        CharacterData("冬", "dōng", "winter", "冬天很冷。(Winter is cold.)", 3),
        CharacterData("暖", "nuǎn", "warm", "今天很暖和。(Today is warm.)", 3),

// 方向与位置
        CharacterData("东", "dōng", "east", "太阳从东方升起。(The sun rises in the east.)", 3),
        CharacterData("西", "xī", "west", "太阳从西边落下。(The sun sets in the west.)", 3),
        CharacterData("南", "nán", "south", "我家在南边。(My home is in the south.)", 3),
        CharacterData("北", "běi", "north", "学校在北边。(The school is in the north.)", 3),
        CharacterData("边", "biān", "side", "河的边上。(By the river side.)", 3),

// 描述词
        CharacterData("长", "cháng", "long", "这条路很长。(This road is very long.)", 3),
        CharacterData("短", "duǎn", "short", "头发很短。(The hair is short.)", 3),
        CharacterData("远", "yuǎn", "far", "家很远。(Home is far away.)", 3),
        CharacterData("近", "jìn", "near", "商店很近。(The shop is near.)", 3),
        CharacterData("轻", "qīng", "light", "这个包很轻。(This bag is light.)", 3),

// 情绪与状态
        CharacterData("累", "lèi", "tired", "我很累。(I am tired.)", 3),
        CharacterData("饿", "è", "hungry", "我很饿。(I am hungry.)", 3),
        CharacterData("渴", "kě", "thirsty", "我很渴。(I am thirsty.)", 3),
        CharacterData("忙", "máng", "busy", "他很忙。(He is busy.)", 3),
        CharacterData("闲", "xián", "free", "我很闲。(I am free.)", 3),

// 学习相关
        CharacterData("问", "wèn", "to ask", "我想问一个问题。(I want to ask a question.)", 3),
        CharacterData("答", "dá", "to answer", "请回答问题。(Please answer the question.)", 3),
        CharacterData("考", "kǎo", "to test", "明天要考试。(We have a test tomorrow.)", 3),
        CharacterData("练", "liàn", "to practice", "要多练习。(Need more practice.)", 3),
        CharacterData("记", "jì", "to remember", "我记住了。(I remember it.)", 3),

// 日常活动
        CharacterData("睡", "shuì", "to sleep", "我要去睡觉。(I want to go to sleep.)", 3),
        CharacterData("醒", "xǐng", "to wake up", "我早上醒了。(I woke up in the morning.)", 3),
        CharacterData("穿", "chuān", "to wear", "穿新衣服。(Wear new clothes.)", 3),
        CharacterData("洗", "xǐ", "to wash", "洗手很重要。(Washing hands is important.)", 3),
        CharacterData("等", "děng", "to wait", "请等一下。(Please wait a moment.)", 3),

// 社交用语
        CharacterData("谢", "xiè", "thank", "谢谢你。(Thank you.)", 3),
        CharacterData("请", "qǐng", "please", "请进。(Please come in.)", 3),
        CharacterData("对", "duì", "correct", "这是对的。(This is correct.)", 3),
        CharacterData("错", "cuò", "wrong", "这是错的。(This is wrong.)", 3),
        CharacterData("抱", "bào", "to hug", "抱歉。(Sorry.)", 3),

// 物品描述
        CharacterData("帘", "lián", "curtain", "帘子很漂亮。(The curtain is beautiful.)", 3),
        CharacterData("毯", "tǎn", "blanket", "毯子很软。(The blanket is soft.)", 3),
        CharacterData("贵", "guì", "expensive", "太贵了。(Too expensive.)", 3),
        CharacterData("便", "pián", "cheap", "很便宜。(Very cheap.)", 3),
        CharacterData("干", "gān", "dry", "衣服干了。(The clothes are dry.)", 3),

// 环境
        CharacterData("树", "shù", "tree", "大树很高。(The tree is tall.)", 3),
        CharacterData("草", "cǎo", "grass", "草是绿的。(The grass is green.)", 3),
        CharacterData("湖", "hú", "lake", "湖水很清。(The lake water is clear.)", 3),
        CharacterData("海", "hǎi", "sea", "大海很蓝。(The sea is blue.)", 3),
        CharacterData("江", "jiāng", "river", "江水很长。(The river is long.)", 3),
        // 第四级汉字（301-400）
// 家庭与日常
        CharacterData("房", "fáng", "house", "房子很大。(The house is big.)", 4),
        CharacterData("墙", "qiáng", "wall", "墙是白色的。(The wall is white.)", 4),
        CharacterData("地", "dì", "floor", "地板很干净。(The floor is clean.)", 4),

// 食物与厨房
        CharacterData("锅", "guō", "pot", "锅里有汤。(There is soup in the pot.)", 4),
        CharacterData("碗", "wǎn", "bowl", "碗是空的。(The bowl is empty.)", 4),
        CharacterData("筷", "kuài", "chopsticks", "用筷子吃饭。(Eat with chopsticks.)", 4),
        CharacterData("盘", "pán", "plate", "盘子很大。(The plate is big.)", 4),
        CharacterData("杯", "bēi", "cup", "杯子里有水。(There is water in the cup.)", 4),

// 衣物与穿着
        CharacterData("衣", "yī", "clothes", "衣服很漂亮。(The clothes are beautiful.)", 4),
        CharacterData("裤", "kù", "pants", "裤子很长。(The pants are long.)", 4),
        CharacterData("鞋", "xié", "shoes", "鞋子很舒服。(The shoes are comfortable.)", 4),
        CharacterData("帽", "mào", "hat", "帽子很时尚。(The hat is stylish.)", 4),

// 交通与出行
        CharacterData("桥", "qiáo", "bridge", "桥很长。(The bridge is long.)", 4),
        CharacterData("站", "zhàn", "station", "车站很近。(The station is near.)", 4),
        CharacterData("票", "piào", "ticket", "票很贵。(The ticket is expensive.)", 4),

// 自然与天气

// 动物与植物

// 颜色与形状
        CharacterData("圆", "yuán", "round", "球是圆的。(The ball is round.)", 4),
        CharacterData("方", "fāng", "square", "盒子是方的。(The box is square.)", 4),

// 运动与休闲
        CharacterData("跃", "yuè", "to leap", "他跃过障碍。(He leaped over the obstacle.)", 4),
        CharacterData("攀", "pān", "to climb", "他在攀岩。(He is rock climbing.)", 4),
        CharacterData("游", "yóu", "to swim", "他在游泳。(He is swimming.)", 4),
        CharacterData("球", "qiú", "ball", "他们在打球。(They are playing ball.)", 4),
        // 第五级汉字（401-500）
// 日常生活
        CharacterData("床", "chuáng", "bed", "床很舒服。(The bed is comfortable.)", 5),
        CharacterData("窗", "chuāng", "window", "窗户很干净。(The window is clean.)", 5),

// 食物与饮料
        CharacterData("糖", "táng", "sugar", "糖很甜。(The sugar is sweet.)", 5),
        CharacterData("盐", "yán", "salt", "盐很咸。(The salt is salty.)", 5),
        CharacterData("油", "yóu", "oil", "油很滑。(The oil is slippery.)", 5),
        CharacterData("酒", "jiǔ", "alcohol", "酒很烈。(The alcohol is strong.)", 5),

// 交通工具
        CharacterData("车", "chē", "car", "车很快。(The car is fast.)", 5),

// 自然现象
        CharacterData("星", "xīng", "star", "星星很亮。(The stars are bright.)", 5),
        CharacterData("雷", "léi", "thunder", "雷声很大。(The thunder is loud.)", 5),
        CharacterData("雾", "wù", "fog", "雾很浓。(The fog is thick.)", 5),

// 动物
        CharacterData("牛", "niú", "cow", "牛在吃草。(The cow is eating grass.)", 5),
        CharacterData("羊", "yáng", "sheep", "羊在山上。(The sheep is on the hill.)", 5),
        CharacterData("猪", "zhū", "pig", "猪在睡觉。(The pig is sleeping.)", 5),
        CharacterData("鸡", "jī", "chicken", "鸡在下蛋。(The chicken is laying eggs.)", 5),
        CharacterData("鸭", "yā", "duck", "鸭在游泳。(The duck is swimming.)", 5),

// 颜色
        CharacterData("绿", "lǜ", "green", "草是绿色的。(The grass is green.)", 5),
        CharacterData("紫", "zǐ", "purple", "花是紫色的。(The flower is purple.)", 5),
        CharacterData("橙", "chéng", "orange", "橙子是橙色的。(The orange is orange.)", 5),
        CharacterData("灰", "huī", "gray", "天空是灰色的。(The sky is gray.)", 5),
        CharacterData("粉", "fěn", "pink", "花是粉色的。(The flower is pink.)", 5),

// 家庭成员
        CharacterData("叔", "shū", "uncle", "叔叔很高。(Uncle is tall.)", 5),
        CharacterData("姨", "yí", "aunt", "姨姨很漂亮。(Aunt is beautiful.)", 5),
        CharacterData("姑", "gū", "aunt", "姑姑很亲切。(Aunt is kind.)", 5),
        CharacterData("舅", "jiù", "uncle", "舅舅很幽默。(Uncle is humorous.)", 5),
        CharacterData("婶", "shěn", "aunt", "婶婶很温柔。(Aunt is gentle.)", 5),

// 运动
        CharacterData("踢", "tī", "to kick", "他在踢球。(He is kicking the ball.)", 5),
        CharacterData("打", "dǎ", "to hit", "他在打篮球。(He is playing basketball.)", 5)
            )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learning)

        // 初始化视图
        characterTextView = findViewById(R.id.characterTextView)
        pinyinTextView = findViewById(R.id.pinyinTextView)
        meaningTextView = findViewById(R.id.meaningTextView)
        exampleTextView = findViewById(R.id.exampleTextView)
        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)

        // 设置自定义字体
        try {
            val typeface = Typeface.createFromAsset(assets, "fonts/simkai.ttf")
            characterTextView.typeface = typeface
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "加载字体失败", Toast.LENGTH_SHORT).show()
        }

        // 设置按钮点击事件
        previousButton.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                updateCharacter()
            } else {
                Toast.makeText(this, "已经是第一个汉字", Toast.LENGTH_SHORT).show()
            }
        }

        nextButton.setOnClickListener {
            if (currentIndex < characters.size - 1) {
                currentIndex++
                updateCharacter()
            } else {
                Toast.makeText(this, "已经是最后一个汉字", Toast.LENGTH_SHORT).show()
            }
        }

        // 显示第一个汉字
        updateCharacter()
    }

    private fun updateCharacter() {
        val character = characters[currentIndex]
        characterTextView.text = character.character
        pinyinTextView.text = character.pinyin
        meaningTextView.text = character.meaning
        exampleTextView.text = character.example
    }
}