# PocketLock 0.2.1: Katzen-Taschenmodus für Android

Native Android-App ohne Werbe-SDK, ohne Internetberechtigung, ohne Tracking.

**Hinweis:** Dieser Ordner enthält den vollständigen Quellcode mit allen Motiven. In der Erstellungsumgebung war kein Android SDK installiert, deshalb ist die APK **noch nicht gebaut oder auf einem Gerät getestet**. Mit dem beiliegenden GitHub-Actions-Workflow oder Android Studio lässt sich eine Debug-APK erzeugen.

## Funktionen

- App-Icon mit der grauen Katze und einem Metallschloss (lokale Grafik).
- Dunkle PocketLock-Startansicht mit Motto **Dein Bildschirm. In Ruhe.**
- Katzenmotiv-Auswahl: Katzen-Schloss, Schlafmütze, Pfote, Balkon, Bernsteinaugen. Vier Originalfotos sind als optimierte WebP-Dateien in die App integriert.
- Ein-Klick-Taschenmodus: undurchsichtiges schwarzes Android-Overlay über normalen Apps.
- Der Touchscreen hinter dem Overlay ist gegen versehentliche Eingaben geschützt. Die Systemleiste bleibt Android-kontrolliert.
- Entsperren: **3 Sekunden still halten und ohne Loslassen nach rechts wischen**. Bei erreichter Haltezeit kurze Vibration, wenn vom Gerät unterstützt.
- Schnellschalter in Android-Schnelleinstellungen, damit andere Apps nicht zum Start verlassen werden müssen.
- Notausgang über Android-Benachrichtigung **Taschenmodus aktiv > Beenden**.
- Keine Fotos, Töne oder Nutzungsdaten werden hochgeladen.

## APK bauen mit GitHub Actions, ohne Android Studio

1. Ein leeres GitHub-Repository erstellen, z. B. `PocketLock-Android`.
2. Den gesamten Projektinhalt aus der ZIP-Datei auf die oberste Ebene des Repositories hochladen, **inklusive** `.github/workflows/build-apk.yml` (versteckter Ordner).
3. Der APK-Build startet beim Hochladen auf den Branch `main` automatisch. Alternativ unter **Actions** den Workflow **PocketLock APK bauen** auswählen und **Run workflow** drücken.
4. Wenn der Workflow erfolgreich grün ist, unter **Releases** die Datei `PocketLock-0.2.1-debug.apk` direkt herunterladen. (Unter Actions/Artifacts gibt es alternativ ein ZIP mit derselben APK.)
5. Android: Datei öffnen, Installation aus dieser Quelle erlauben, App installieren.

Die Release-Erstellung erfordert GitHub-Actions-Berechtigung `contents: write`. Falls Releases durch Repo-Einstellungen gesperrt sind, liegt die APK weiterhin unter den Workflow-Artefakten.

## APK bauen mit Android Studio

Android Studio mit Android SDK 35 und JDK 17 installieren.

**Windows:** einmalig PowerShell-Skript `setup-wrapper-windows.ps1` starten, damit die offizielle Gradle-Wrapper-JAR heruntergeladen wird. Projekt in Android Studio öffnen und **Build > Build APK(s)** wählen.

**Mac/Linux:** `gradlew` lädt den Wrapper bei Bedarf nach, ansonsten Gradle 8.11.1 installieren und `gradle :app:assembleDebug` ausführen.

Danach liegt die Debug-APK unter `app/build/outputs/apk/debug/app-debug.apk`.

## Bedienung

1. PocketLock öffnen, bei Aufforderung **Über anderen Apps einblenden** erlauben.
2. **Taschenmodus starten** antippen. Das Overlay verdeckt den Bildschirm und fängt Berührungen ab.
3. Auf Schwarzfläche einen Finger **3 Sekunden still halten**, danach ohne Loslassen **nach rechts wischen**.
4. Alternativ Notausgang über die PocketLock-Benachrichtigung. Schnellschalter optional einrichten.

## Technische Grenzen und Sicherheit

- Es handelt sich **nicht** um die reguläre Android-Bildschirmsperre und **nicht** um eine Methode, Gerätedaten vor anderen zu schützen.
- Androids Statusleiste, Systemgesten, eingehende Anrufe, kritische Dialoge und Hardwaretasten lassen sich nicht vollständig blockieren.
- Bei schwarzem Overlay ist das Display **elektrisch weiter an**. Insbesondere mit einem LCD kann die Energieaufnahme hoch bleiben. Vor Nutzung in der Tasche Gerätetemperatur und Akku kontrollieren.
- Eine bereits aktive App kann weiterlaufen, **wenn Android und diese App das zulassen**. Laufendes Video oder Kamera können pausieren; eine Garantie für uneingeschränkte Hintergrundausführung gibt es nicht.
- Android 15/16 kann den Start eines Foreground Service aus dem Hintergrund einschränken; der Schnellschalter ist daher auf einem realen Android-Gerät gesondert zu testen.
- Ab Android 13 fragt die App vor dem Start einmalig die Benachrichtigungsberechtigung an. Wird sie verweigert, funktioniert die Entsperrgeste weiterhin, der Notausgang über die Benachrichtigung steht aber möglicherweise nicht zur Verfügung.
- Die App benötigt keine Berechtigung für Fotos, Mikrofon, Internet oder Speicher.
- `specialUse`-Foreground-Service erfordert vor einer Veröffentlichung im Google Play Store eine Erklärung.
- Debug-APK eignet sich für eigene Tests, nicht als abschließend getestete Store-Version.

## Änderungen in 0.2.1

- Der Start wartet jetzt die Android-13+-Benachrichtigungsberechtigung ab, statt das Fenster während der Berechtigungsfrage zu schließen.
- Der GitHub-Workflow startet beim Upload auf `main` automatisch, zusätzlich zur manuellen Ausführung.
- Das bestehende GitHub-Projekt ClipBridge wurde **nicht** verändert.

## Noch nicht geprüft

- [ ] Erfolgreicher Gradle-Build mit Android SDK 35
- [ ] Android-Gerätetest, besonders für Android 16
- [ ] Start/Stop aus Schnelleinstellungen
- [ ] Korrekte Berührungsabdeckung auf Geräten mit Gestennavigation
- [ ] Verhalten bei Anrufen, Bildschirm ausschalten, Akkusparmodus
- [ ] Langzeittest mit laufendem Audio/Video und Erwärmung
