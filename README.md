# Minecraft Teams Plugin (TeamsPlugin)

Takım tabanlı kule savaşları için geliştirilmiş, bölge korumalı bir Spigot eklentisi. Oyuncular takımlara ayrılır, takımlara ait bölgeler tanımlanır ve bir bölgede yalnızca sahibi olan takımın üyeleri blok koyup kırabilir.

*A Spigot plugin for team-based tower wars with team-owned, protected regions. English version below.*

**İndir / Download:** [Releases](https://github.com/CaYatur/minecraft-teams-plugin/releases) · [v1.0](https://github.com/CaYatur/minecraft-teams-plugin/releases/tag/v1.0)

---

## 🇹🇷 Türkçe

### Özellikler
- Takım oluşturma (8 renkten biriyle), takıma katılma/ayrılma, başka oyuncuyu takıma ekleme/çıkarma.
- İki köşe seçerek takıma ait bölge oluşturma; bölge her yönde otomatik olarak **3 blok** genişletilir.
- Bölge koruması: başka takımın bölgesinde blok koyma, kırma, kova ile su/lav dökme ve çakmakla TNT yakma engellenir.
- Bölge silme, bilgi görme, listeleme ve sahipliği başka takıma devretme.
- Bölge sınırlarını takım renginde parçacıklarla görselleştirme (sadece kendine veya tüm oyunculara, süreli veya sonsuz).
- Takım ve bölge verilerinin YAML dosyalarında saklanması; oyuncular UUID ile tutulur, sunucudan çıkınca takımda kalırlar.
- Hiç takım yoksa ilk açılışta varsayılan **Kırmızı** (RED) ve **Mavi** (BLUE) takımları oluşturulur.
- Komutlarda sekme (Tab) ile otomatik tamamlama.
- Oyun içi mesajlar Türkçedir.

### Gereksinimler
| | |
|---|---|
| Sunucu | Spigot (veya Spigot API'sini destekleyen Paper vb.) |
| Derlendiği API | `spigot-api 1.20.4-R0.1-SNAPSHOT` (`pom.xml`) |
| `api-version` | `1.20` (`plugin.yml`) |
| Java | 17 |

> Not: Görselleştirme `Particle.REDSTONE` kullanır. Bu sabit 1.20.5 ve sonrasında `DUST` olarak yeniden adlandırıldığı için eklenti 1.20–1.20.4 için hedeflenmiştir; daha yeni sürümlerde test edilmemiştir.

### Kurulum
1. [Releases](https://github.com/CaYatur/minecraft-teams-plugin/releases) sayfasından `minecraft-teams-plugin-1.0.jar` dosyasını indirin.
2. Dosyayı sunucunuzun `plugins/` klasörüne koyun.
3. Sunucuyu yeniden başlatın.

### Komutlar
| Komut | Takma adlar | Açıklama |
|---|---|---|
| `/cyteam` | `/t`, `/team` | Takım yönetim komutları |
| `/cyregion` | `/r`, `/region` | Bölge yönetim komutları |
| `/cyhelp` | — | Eklenti yardım menüsü |

#### `/cyteam` alt komutları
| Kullanım | Açıklama |
|---|---|
| `/cyteam help` (veya argümansız `/cyteam`) | Takım komutları yardım menüsünü gösterir. |
| `/cyteam create <takım-adı> <renk>` | Yeni takım oluşturur. Renkler: `RED`, `BLUE`, `GREEN`, `YELLOW`, `PURPLE`, `AQUA`, `GOLD`, `GRAY` (büyük/küçük harf fark etmez). Aynı adda takım varsa reddedilir. |
| `/cyteam join <takım-adı> [oyuncu]` | Kendinizi ya da belirtilen çevrimiçi oyuncuyu takıma ekler. Oyuncu zaten bir takımdaysa reddedilir. Konsoldan kullanılırken oyuncu belirtilmelidir. |
| `/cyteam leave [oyuncu]` | Kendinizi ya da belirtilen çevrimiçi oyuncuyu takımından çıkarır. |
| `/cyteam info [takım-adı]` | Takımın rengini ve çevrimiçi oyuncularını gösterir. Ad verilmezse kendi takımınızı gösterir. |
| `/cyteam list` | Tüm takımları ve oyuncu sayılarını listeler. |

`/cyteam` konsoldan da kullanılabilir.

#### `/cyregion` alt komutları (yalnızca oyuncular)
| Kullanım | Açıklama |
|---|---|
| `/cyregion help` (veya argümansız `/cyregion`) | Bölge komutları yardım menüsünü gösterir. |
| `/cyregion select <1\|2>` | Durduğunuz bloğu 1. veya 2. köşe olarak seçer. İki köşe seçilince alan boyutu ve 3 blok genişletilmiş korunan alan gösterilir. |
| `/cyregion create <bölge-adı> <takım-adı>` | Seçili iki köşe arasında, belirtilen takıma ait bölge oluşturur (her yönde +3 blok). |
| `/cyregion delete <bölge-adı>` | Bölgeyi siler. |
| `/cyregion info [bölge-adı]` | Bölgenin sahibini, hacmini ve köşelerini gösterir. Ad verilmezse içinde bulunduğunuz bölgeyi gösterir. |
| `/cyregion list` | Tüm bölgeleri sahip takım ve hacimleriyle listeler. |
| `/cyregion transfer <bölge-adı> <takım-adı>` | Bölgenin sahipliğini başka takıma devreder. |
| `/cyregion visualize <bölge-adı> [all\|global] [süre\|sonsuz]` | Bölgeyi parçacıklarla gösterir. |
| `/cyregion visualizeall [all\|global] [süre\|sonsuz]` | Tüm bölgeleri gösterir. |
| `/cyregion visualizeteam <takım-adı> [all\|global] [süre\|sonsuz]` | Bir takımın tüm bölgelerini gösterir. |
| `/cyregion stopvisualize [bölge-adı\|all]` | Ad verilmezse kendi görselleştirmenizi, bölge adıyla o bölgenin genel görselleştirmesini, `all` ile tüm görselleştirmeleri durdurur. |

Görselleştirme ayrıntıları: varsayılan süre **30 saniye**; süre 1–3600 saniye olabilir, `sonsuz` veya `infinite` süresiz gösterir. `all`/`global` verilirse herkese, verilmezse yalnızca komutu kullanana gösterilir. Parçacıklar yarım saniyede bir, takım renginde çizilir (`GOLD` ve `GRAY` beyaz görünür).

#### `/cyhelp`
`/cyteam help` ve `/cyregion help` komutlarını tanıtan kısa bir genel yardım gösterir.

### İzinler
`plugin.yml` içinde **hiç izin (permission) tanımlı değildir** ve kodda izin kontrolü yoktur. Bu nedenle tüm komutlar (bölge silme ve devretme dahil) **her oyuncu tarafından kullanılabilir**. Erişimi kısıtlamak isterseniz LuckPerms gibi bir izin eklentisiyle komutları engellemeniz gerekir.

### Takımlar ve bölgeler nasıl çalışır
- **Takımlar:** Takım adları büyük/küçük harf duyarsızdır. Bir oyuncu aynı anda yalnızca bir takımda olabilir. Takımın lideri yoktur; takımlar komutlarla yönetilir. Oyuncular UUID ile saklanır; sunucudan çıkan oyuncu takımda kalır.
- **Bölge oluşturma:** `select 1` ve `select 2` ile iki köşe seçilir, `create` ile bölge oluşturulur. Seçilen bloklar dahil edilir ve bölge her yönde (yukarı/aşağı dahil) 3 blok genişletilir. Bölge adları büyük/küçük harf duyarsızdır ve benzersiz olmalıdır. Bölge, seçimin yapıldığı dünyaya bağlıdır.
- **Sahiplik:** Her bölgenin sahibi bir takımdır; `transfer` ile değiştirilebilir.
- **Koruma kuralları (`BlockListener`):** Bir konum herhangi bir bölgenin içindeyse, yalnızca o bölgenin sahibi takımın üyeleri blok koyabilir, blok kırabilir, kova ile su/lav dökebilir ve çakmakla TNT yakabilir (tıklanan blok veya üstündeki blok TNT ise). Takımı olmayan oyuncular hiçbir bölgede bunları yapamaz. Bölge dışında kısıtlama yoktur.
- **Oyuncu olayları (`PlayerListener`):** Oyuncu girişte bir karşılama mesajı ve takımının adını görür; takımı yoksa `/cyteam join` önerilir. Çıkışta takım üyeliği silinmez.

### Veri saklama
Veriler `plugins/TeamsPlugin/` klasöründe tutulur (ayrı bir `config.yml` yoktur):

| Dosya | İçerik |
|---|---|
| `teamList.yml` | Takım listesi |
| `teams/<takım>.yml` | `name`, `color`, `players` (UUID listesi) |
| `regionList.yml` | Bölge listesi |
| `regions/<bölge>.yml` | `name`, `worldName`, `minX/minY/minZ`, `maxX/maxY/maxZ`, `ownerTeam` |

Takım verileri `join`/`leave` komutlarından sonra ve sunucu kapanırken kaydedilir. Bölgeler ve yeni oluşturulan takımlar yalnızca **eklenti kapanırken** (sunucu düzgün kapatıldığında) kaydedilir. Veriler açılışta yüklenir.

### Bilinen sorunlar
- `plugin.yml` içindeki `main: com.cayadev.teamsplugin.TeamsPlugin`, Java sınıflarının paketiyle (`com.example.teamsplugin`) uyuşmuyor. Bu haliyle sunucu ana sınıfı bulamayabilir; `main` değerinin `com.example.teamsplugin.TeamsPlugin` olarak düzeltilmesi gerekebilir.
- Sunucu çökerse son düzgün kapanıştan sonra oluşturulan bölgeler kaybolabilir.
- Silinen bölgelerin `regions/*.yml` dosyaları diskten silinmez (tekrar yüklenmezler).

### Kaynaktan derleme
Gereksinimler: JDK 17 ve Maven.
```bash
mvn clean package
```
Çıktı: `target/minecraft-teams-plugin-1.0.jar`

### Proje yapısı
```
pom.xml
src/main/resources/plugin.yml
src/main/java/com/example/teamsplugin/
├── TeamsPlugin.java            # Ana sınıf: yükleme, komut/olay kaydı, kayıt
├── commands/
│   ├── HelpCommand.java        # /cyhelp
│   ├── TeamCommands.java       # /cyteam + Tab tamamlama
│   └── RegionCommands.java     # /cyregion + Tab tamamlama
├── listeners/
│   ├── BlockListener.java      # Blok/kova/TNT koruması
│   └── PlayerListener.java     # Giriş/çıkış olayları
├── managers/
│   ├── TeamManager.java        # Takımlar ve YAML kayıt/yükleme
│   └── RegionManager.java      # Bölgeler, seçimler, görselleştirme, kayıt/yükleme
└── models/
    ├── Team.java
    └── Region.java
```

### Proje tarihleri / Project dates
| | |
|---|---|
| Oluşturulma / Created | 6 Haziran 2025 (6 June 2025) — en eski kaynak dosya / oldest source file |
| Son güncelleme / Last updated | 6 Haziran 2025 (6 June 2025) — en yeni kaynak dosya / newest source file (`plugin.yml`) |

### Lisans
Bu proje için henüz bir lisans belirtilmemiştir.

---

## 🇬🇧 English

### Overview
A Spigot plugin for team-based tower wars. Players are split into teams, teams own regions, and inside a region only members of the owning team can build or break. In-game messages are in Turkish.

### Features
- Create teams with one of 8 colors, join/leave, add or remove other players.
- Create team-owned regions from two selected corners; the region is automatically expanded by **3 blocks** in every direction.
- Protection: block placing, block breaking, emptying water/lava buckets and igniting TNT with flint and steel are blocked in other teams' regions.
- Delete, inspect, list and transfer regions.
- Visualize region borders with particles in the team color (to yourself or everyone, timed or infinite).
- Teams and regions stored in YAML; players stored by UUID and keep their team after leaving.
- If no teams exist on startup, default teams **Kırmızı** (RED) and **Mavi** (BLUE) are created.
- Tab completion for commands.

### Requirements
- Server: Spigot (or Spigot-API compatible servers such as Paper)
- Built against `spigot-api 1.20.4-R0.1-SNAPSHOT`, `api-version: '1.20'`
- Java 17
- Visualization uses `Particle.REDSTONE` (renamed `DUST` in 1.20.5), so the target is 1.20–1.20.4; newer versions are untested.

### Installation
1. Download `minecraft-teams-plugin-1.0.jar` from [Releases](https://github.com/CaYatur/minecraft-teams-plugin/releases).
2. Put it into your server's `plugins/` folder.
3. Restart the server.

### Commands
| Command | Aliases | Description |
|---|---|---|
| `/cyteam` | `/t`, `/team` | Team management |
| `/cyregion` | `/r`, `/region` | Region management (players only) |
| `/cyhelp` | — | General help |

**`/cyteam`**
- `help` – show team help (also with no arguments)
- `create <team> <color>` – colors: `RED`, `BLUE`, `GREEN`, `YELLOW`, `PURPLE`, `AQUA`, `GOLD`, `GRAY`
- `join <team> [player]` – join yourself or add an online player (console must name a player); rejected if already in a team
- `leave [player]` – leave your team or remove an online player
- `info [team]` – color and online members; defaults to your own team
- `list` – all teams with player counts

**`/cyregion`**
- `help` – show region help (also with no arguments)
- `select <1|2>` – set corner 1 or 2 at the block you stand on
- `create <region> <team>` – create a region from the selection for that team (+3 blocks each way)
- `delete <region>` – delete a region
- `info [region]` – owner, volume, corners; defaults to the region you are in
- `list` – all regions with owner and volume
- `transfer <region> <team>` – change the owner
- `visualize <region> [all|global] [seconds|sonsuz]`
- `visualizeall [all|global] [seconds|sonsuz]`
- `visualizeteam <team> [all|global] [seconds|sonsuz]`
- `stopvisualize [region|all]` – stop your own, one region's global, or all visualizations

Visualization defaults to 30 seconds; 1–3600 seconds allowed, `sonsuz`/`infinite` means no limit. `all`/`global` shows it to every online player. `GOLD` and `GRAY` teams are drawn in white.

### Permissions
No permissions are defined in `plugin.yml` and the code performs no permission checks, so **every player can use every command**, including region delete and transfer. Use a permissions plugin to restrict commands if needed.

### How teams and regions work
- Team and region names are case-insensitive. A player can be in only one team. Teams have no leader.
- A region includes the selected blocks plus 3 blocks in every direction (including up/down) and belongs to the world it was selected in.
- Inside any region, only members of the owning team can place/break blocks, empty buckets or ignite TNT (when the clicked block or the block above it is TNT). Players without a team cannot do these in any region. Outside regions there are no restrictions.
- On join players get a welcome message and their team name (or a hint to use `/cyteam join`). On quit they stay in their team.

### Data storage
Stored in `plugins/TeamsPlugin/` (no `config.yml`): `teamList.yml`, `teams/<team>.yml`, `regionList.yml`, `regions/<region>.yml`. Team data is saved after `join`/`leave` and on shutdown; regions and newly created teams are saved **only when the plugin is disabled** (clean shutdown).

### Known issues
- `plugin.yml` declares `main: com.cayadev.teamsplugin.TeamsPlugin`, but the classes are in `com.example.teamsplugin`. The server may fail to find the main class; `main` may need to be changed to `com.example.teamsplugin.TeamsPlugin`.
- Regions created since the last clean shutdown can be lost on a crash.
- Files of deleted regions stay in `regions/` (they are not loaded again).

### Building from source
Requires JDK 17 and Maven: `mvn clean package` → `target/minecraft-teams-plugin-1.0.jar`

### Project structure
See the tree in the Turkish section above.

### License
No license has been specified for this project yet. See the project dates in the section above.

### Links
- [Releases](https://github.com/CaYatur/minecraft-teams-plugin/releases)
