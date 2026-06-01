// OSINT V — Полное приложение для сбора данных о человеке
// Весь код приложения в одном файле main.dart
// Зависимости в pubspec.yaml

import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:csv/csv.dart';
import 'package:excel/excel.dart' as xls;
import 'package:file_picker/file_picker.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:graphview/GraphView.dart';
import 'package:latlong2/latlong.dart';
import 'package:open_filex/open_filex.dart';
import 'package:path_provider/path_provider.dart';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:printing/printing.dart';
import 'package:share_plus/share_plus.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:sqflite/sqflite.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:uuid/uuid.dart';

// ============================================================================
// MAIN
// ============================================================================

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await AppState.instance.init();
  runApp(const OsintApp());
}

// ============================================================================
// LOCALIZATION
// ============================================================================

class L10n {
  static const Map<String, Map<String, String>> _t = {
    'en': {
      'app_title': 'OSINT V',
      'targets': 'Targets',
      'new_target': 'New target',
      'edit_target': 'Edit target',
      'name': 'Name',
      'surname': 'Surname',
      'patronymic': 'Patronymic',
      'notes': 'Notes',
      'tags': 'Tags',
      'add_tag': 'Add tag',
      'tag_name': 'Tag name',
      'deep_search': 'Deep search...',
      'no_targets': 'No targets yet. Tap + to add one.',
      'no_results': 'No results.',
      'settings': 'Settings',
      'theme': 'Theme',
      'light': 'Light',
      'dark': 'Dark',
      'amoled': 'AMOLED',
      'language': 'Language',
      'storage_path': 'Storage path',
      'export_json': 'Export JSON',
      'import_json': 'Import JSON',
      'export_db': 'Export database (SQLite .db)',
      'about': 'About',
      'disclaimer':
          'The author bears no responsibility for the use of this application. '
              'All data is stored locally on your device. Use it lawfully.',
      'tutorial': 'Tutorial',
      'tutorial_text':
          '• Marks: in settings add a single character (e.g. "@") and any value beginning with it gets a copy button. Built-in marks always work for: phone numbers (+380 635 875 132), card numbers (16 digits), coordinates (lat, lng e.g. 50.45, 30.52).\n'
              '• Map: enter coordinates as the value (e.g. 50.4501, 30.5234). The app detects them and shows a map button.\n'
              '• Phones: enter with spaces and "+" — any format with country code is recognized.\n'
              '• Categories: tap "+ Category" inside Info tab. Inside a category add key-value pairs. Long-press to drag and reorder.\n'
              '• Connections: open a target → Connections tab → add another target and the reasons. Tap a connection target to open them.',
      'custom_marks': 'Custom marks',
      'custom_marks_subtitle': 'Manage custom copy-marks',
      'no_marks_yet': 'No custom marks yet.',
      'add_mark': 'Add mark',
      'mark_char': 'Mark character',
      'mark_label': 'Label (optional)',
      'info': 'Info',
      'connections': 'Connections',
      'evidence': 'Evidence',
      'map': 'Map',
      'add_category': 'Add category',
      'category_name': 'Category name',
      'add_kv': 'Add key-value',
      'key': 'Key',
      'value': 'Value',
      'add_connection': 'Add connection',
      'select_person': 'Select person',
      'reasons': 'Reasons (one per line)',
      'add_evidence': 'Add evidence',
      'pick_files': 'Pick files',
      'description': 'Description',
      'generate_pdf': 'Generate PDF',
      'pdf_options': 'PDF options',
      'include_connections': 'Include connections',
      'include_evidence': 'Include evidence',
      'preview': 'Preview',
      'share': 'Share',
      'save': 'Save',
      'cancel': 'Cancel',
      'delete': 'Delete',
      'confirm_delete': 'Delete this item?',
      'this_action_cannot_be_undone': 'This action cannot be undone.',
      'edit': 'Edit',
      'open': 'Open',
      'copy': 'Copy',
      'copied': 'Copied',
      'open_on_map': 'Open on map',
      'all_targets_table': 'All targets table',
      'export_csv': 'Export CSV',
      'export_excel': 'Export Excel',
      'export_md': 'Export MD',
      'export_pdf': 'Export PDF',
      'graph': 'Connections graph',
      'graph_filter': 'Filter by reason',
      'all_reasons': 'All reasons',
      'pick_reason': 'Pick reason',
      'no_connections_yet': 'No connections yet.',
      'no_evidence_yet': 'No evidence yet.',
      'no_categories_yet': 'No categories yet.',
      'attached_files': 'Attached files',
      'add': 'Add',
      'remove': 'Remove',
      'rename': 'Rename',
      'sure_delete': 'Are you sure?',
      'connections_count': 'connections',
      'no_target_marker': 'No coordinates found in this target.',
      'go_to_marker': 'Open',
      'storage_dir_info': 'Application documents directory',
      'reset_db': 'Reset database',
      'reset_confirm': 'This deletes ALL targets, categories and evidence.',
      'reasons_csv': 'Reasons',
      'pdf_only_self': 'Only this target',
      'pdf_with_links': 'With connections',
      'menu': 'Menu',
      'home': 'Home',
      'snack_imported': 'Imported',
      'snack_exported': 'Exported',
      'snack_saved': 'Saved',
      'snack_deleted': 'Deleted',
      'no_name': '(no name)',
      'rename_category': 'Rename category',
      'choose_target': 'Choose target',
    },
    'ru': {
      'app_title': 'OSINT V',
      'targets': 'Цели',
      'new_target': 'Новая цель',
      'edit_target': 'Редактировать цель',
      'name': 'Имя',
      'surname': 'Фамилия',
      'patronymic': 'Отчество',
      'notes': 'Заметки',
      'tags': 'Теги',
      'add_tag': 'Добавить тег',
      'tag_name': 'Название тега',
      'deep_search': 'Глубокий поиск...',
      'no_targets': 'Пока нет целей. Нажмите +, чтобы добавить.',
      'no_results': 'Ничего не найдено.',
      'settings': 'Настройки',
      'theme': 'Тема',
      'light': 'Светлая',
      'dark': 'Тёмная',
      'amoled': 'AMOLED',
      'language': 'Язык',
      'storage_path': 'Путь хранилища',
      'export_json': 'Экспорт JSON',
      'import_json': 'Импорт JSON',
      'export_db': 'Выгрузить базу (SQLite .db)',
      'about': 'О приложении',
      'disclaimer':
          'Автор не несёт ответственности за использование данного приложения. '
              'Все данные хранятся локально на вашем устройстве. Используйте законно.',
      'tutorial': 'Туториал',
      'tutorial_text':
          '• Пометки: в настройках добавьте символ (например "@") и любое значение, начинающееся с него, получит кнопку копирования. Встроенные пометки работают всегда для: номеров телефона (+380 635 875 132), номеров карт (16 цифр), координат (широта, долгота, например 50.45, 30.52).\n'
              '• Карта: вводите координаты как значение (например 50.4501, 30.5234). Программа их определит и покажет кнопку открытия на карте.\n'
              '• Телефоны: вводите с пробелами и "+" — распознаётся любой формат с кодом страны.\n'
              '• Категории: нажмите "+ Категория" во вкладке Инфо. Внутри категории добавляйте ключ-значение. Зажмите для перетаскивания и сортировки.\n'
              '• Связи: откройте цель → вкладка Связи → добавьте другую цель и причины. Нажмите на цель внутри связи, чтобы её открыть.',
      'custom_marks': 'Свои пометки',
      'custom_marks_subtitle': 'Управление пометками копирования',
      'no_marks_yet': 'Пометок пока нет.',
      'add_mark': 'Добавить пометку',
      'mark_char': 'Символ пометки',
      'mark_label': 'Подпись (необязательно)',
      'info': 'Инфо',
      'connections': 'Связи',
      'evidence': 'Доказательства',
      'map': 'Карта',
      'add_category': 'Добавить категорию',
      'category_name': 'Название категории',
      'add_kv': 'Добавить ключ-значение',
      'key': 'Ключ',
      'value': 'Значение',
      'add_connection': 'Добавить связь',
      'select_person': 'Выберите персону',
      'reasons': 'Причины (по одной в строке)',
      'add_evidence': 'Добавить доказательство',
      'pick_files': 'Выбрать файлы',
      'description': 'Описание',
      'generate_pdf': 'Сгенерировать PDF',
      'pdf_options': 'Параметры PDF',
      'include_connections': 'Включить связи',
      'include_evidence': 'Включить доказательства',
      'preview': 'Предпросмотр',
      'share': 'Поделиться',
      'save': 'Сохранить',
      'cancel': 'Отмена',
      'delete': 'Удалить',
      'confirm_delete': 'Удалить элемент?',
      'this_action_cannot_be_undone': 'Это действие нельзя отменить.',
      'edit': 'Редактировать',
      'open': 'Открыть',
      'copy': 'Копировать',
      'copied': 'Скопировано',
      'open_on_map': 'Открыть на карте',
      'all_targets_table': 'Полная таблица целей',
      'export_csv': 'Экспорт CSV',
      'export_excel': 'Экспорт Excel',
      'export_md': 'Экспорт MD',
      'export_pdf': 'Экспорт PDF',
      'graph': 'Граф связей',
      'graph_filter': 'Фильтр по причине',
      'all_reasons': 'Все причины',
      'pick_reason': 'Выбрать причину',
      'no_connections_yet': 'Связей пока нет.',
      'no_evidence_yet': 'Доказательств пока нет.',
      'no_categories_yet': 'Категорий пока нет.',
      'attached_files': 'Прикреплённые файлы',
      'add': 'Добавить',
      'remove': 'Удалить',
      'rename': 'Переименовать',
      'sure_delete': 'Точно удалить?',
      'connections_count': 'связей',
      'no_target_marker': 'В этой цели нет координат.',
      'go_to_marker': 'Открыть',
      'storage_dir_info': 'Папка документов приложения',
      'reset_db': 'Сбросить базу',
      'reset_confirm': 'Это удалит ВСЕ цели, категории и доказательства.',
      'reasons_csv': 'Причины',
      'pdf_only_self': 'Только эта цель',
      'pdf_with_links': 'Со связями',
      'menu': 'Меню',
      'home': 'Главная',
      'snack_imported': 'Импортировано',
      'snack_exported': 'Экспортировано',
      'snack_saved': 'Сохранено',
      'snack_deleted': 'Удалено',
      'no_name': '(без имени)',
      'rename_category': 'Переименовать категорию',
      'choose_target': 'Выберите цель',
    },
  };

  static String t(String key) {
    final lang = AppState.instance.settings.language;
    return _t[lang]?[key] ?? _t['en']![key] ?? key;
  }
}

String tr(String key) => L10n.t(key);

// ============================================================================
// MODELS
// ============================================================================

const _uuid = Uuid();

class KeyValue {
  String id;
  String key;
  String value;
  KeyValue({String? id, this.key = '', this.value = ''})
      : id = id ?? _uuid.v4();
  Map<String, dynamic> toJson() => {'id': id, 'k': key, 'v': value};
  factory KeyValue.fromJson(Map<String, dynamic> j) =>
      KeyValue(id: j['id'], key: j['k'] ?? '', value: j['v'] ?? '');
}

class CategoryBlock {
  String id;
  String name;
  List<KeyValue> entries;
  CategoryBlock({String? id, this.name = '', List<KeyValue>? entries})
      : id = id ?? _uuid.v4(),
        entries = entries ?? [];
  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'entries': entries.map((e) => e.toJson()).toList(),
      };
  factory CategoryBlock.fromJson(Map<String, dynamic> j) => CategoryBlock(
        id: j['id'],
        name: j['name'] ?? '',
        entries: ((j['entries'] as List?) ?? [])
            .map((e) => KeyValue.fromJson(e))
            .toList(),
      );
}

class ConnectionLink {
  String id;
  String targetPersonId;
  List<String> reasons;
  ConnectionLink({String? id, required this.targetPersonId, List<String>? reasons})
      : id = id ?? _uuid.v4(),
        reasons = reasons ?? [];
  Map<String, dynamic> toJson() =>
      {'id': id, 'pid': targetPersonId, 'reasons': reasons};
  factory ConnectionLink.fromJson(Map<String, dynamic> j) => ConnectionLink(
        id: j['id'],
        targetPersonId: j['pid'] ?? '',
        reasons: ((j['reasons'] as List?) ?? []).cast<String>(),
      );
}

class EvidenceItem {
  String id;
  String description;
  List<String> filePaths; // absolute paths copied into app docs dir
  EvidenceItem({String? id, this.description = '', List<String>? filePaths})
      : id = id ?? _uuid.v4(),
        filePaths = filePaths ?? [];
  Map<String, dynamic> toJson() =>
      {'id': id, 'desc': description, 'files': filePaths};
  factory EvidenceItem.fromJson(Map<String, dynamic> j) => EvidenceItem(
        id: j['id'],
        description: j['desc'] ?? '',
        filePaths: ((j['files'] as List?) ?? []).cast<String>(),
      );
}

class Person {
  String id;
  String name;
  String surname;
  String patronymic;
  String notes;
  List<String> tags;
  List<CategoryBlock> categories;
  List<ConnectionLink> connections;
  List<EvidenceItem> evidence;

  Person({
    String? id,
    this.name = '',
    this.surname = '',
    this.patronymic = '',
    this.notes = '',
    List<String>? tags,
    List<CategoryBlock>? categories,
    List<ConnectionLink>? connections,
    List<EvidenceItem>? evidence,
  })  : id = id ?? _uuid.v4(),
        tags = tags ?? [],
        categories = categories ?? [],
        connections = connections ?? [],
        evidence = evidence ?? [];

  String get fullName {
    // Display order matches the input panel: Имя Фамилия Отчество.
    final parts = [name, surname, patronymic].where((s) => s.isNotEmpty).toList();
    return parts.isEmpty ? tr('no_name') : parts.join(' ');
  }

  String get initials {
    String firstChar(String s) => s.isEmpty ? '' : s[0].toUpperCase();
    final s = '${firstChar(name)}${firstChar(surname)}';
    return s.isEmpty ? '?' : s;
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'surname': surname,
        'patronymic': patronymic,
        'notes': notes,
        'tags': tags,
        'categories': categories.map((e) => e.toJson()).toList(),
        'connections': connections.map((e) => e.toJson()).toList(),
        'evidence': evidence.map((e) => e.toJson()).toList(),
      };

  factory Person.fromJson(Map<String, dynamic> j) => Person(
        id: j['id'],
        name: j['name'] ?? '',
        surname: j['surname'] ?? '',
        patronymic: j['patronymic'] ?? '',
        notes: j['notes'] ?? '',
        tags: ((j['tags'] as List?) ?? []).cast<String>(),
        categories: ((j['categories'] as List?) ?? [])
            .map((e) => CategoryBlock.fromJson(e))
            .toList(),
        connections: ((j['connections'] as List?) ?? [])
            .map((e) => ConnectionLink.fromJson(e))
            .toList(),
        evidence: ((j['evidence'] as List?) ?? [])
            .map((e) => EvidenceItem.fromJson(e))
            .toList(),
      );

  Iterable<String> searchHaystack() sync* {
    yield name;
    yield surname;
    yield patronymic;
    yield notes;
    for (final t in tags) yield t;
    for (final c in categories) {
      yield c.name;
      for (final kv in c.entries) {
        yield kv.key;
        yield kv.value;
      }
    }
    for (final e in evidence) yield e.description;
    for (final c in connections) {
      for (final r in c.reasons) yield r;
    }
  }
}

class CustomMark {
  String char;
  String label;
  CustomMark({required this.char, this.label = ''});
  Map<String, dynamic> toJson() => {'c': char, 'l': label};
  factory CustomMark.fromJson(Map<String, dynamic> j) =>
      CustomMark(char: j['c'] ?? '', label: j['l'] ?? '');
}

enum AppTheme { light, dark, amoled }

class Settings {
  AppTheme theme;
  String language;
  List<CustomMark> marks;

  Settings({
    this.theme = AppTheme.dark,
    this.language = 'ru',
    List<CustomMark>? marks,
  }) : marks = marks ?? [];

  Map<String, dynamic> toJson() => {
        'theme': theme.name,
        'language': language,
        'marks': marks.map((e) => e.toJson()).toList(),
      };
  factory Settings.fromJson(Map<String, dynamic> j) => Settings(
        theme: AppTheme.values.firstWhere(
          (t) => t.name == (j['theme'] ?? 'dark'),
          orElse: () => AppTheme.dark,
        ),
        language: j['language'] ?? 'ru',
        marks: ((j['marks'] as List?) ?? [])
            .map((e) => CustomMark.fromJson(e))
            .toList(),
      );
}

// ============================================================================
// APP STATE
// ============================================================================

class AppState extends ChangeNotifier {
  AppState._();
  static final AppState instance = AppState._();

  late Directory docsDir;
  late File _dataFile;
  late SharedPreferences _prefs;

  Settings settings = Settings();
  List<Person> people = [];

  Future<void> init() async {
    docsDir = await getApplicationDocumentsDirectory();
    _dataFile = File('${docsDir.path}/osint_v_data.json');
    _prefs = await SharedPreferences.getInstance();

    final settingsJson = _prefs.getString('settings');
    if (settingsJson != null) {
      try {
        settings = Settings.fromJson(jsonDecode(settingsJson));
      } catch (_) {}
    }

    if (await _dataFile.exists()) {
      try {
        final raw = await _dataFile.readAsString();
        final data = jsonDecode(raw) as Map<String, dynamic>;
        people = ((data['people'] as List?) ?? [])
            .map((e) => Person.fromJson(e))
            .toList();
      } catch (_) {}
    }
  }

  Future<void> persist() async {
    final data = {'people': people.map((p) => p.toJson()).toList()};
    await _dataFile.writeAsString(jsonEncode(data));
    await _prefs.setString('settings', jsonEncode(settings.toJson()));
    notifyListeners();
  }

  Future<void> persistSettingsOnly() async {
    await _prefs.setString('settings', jsonEncode(settings.toJson()));
    notifyListeners();
  }

  String get dataFilePath => _dataFile.path;

  Person? findById(String id) {
    for (final p in people) {
      if (p.id == id) return p;
    }
    return null;
  }

  Future<File> exportJsonFile() async {
    final f = File('${docsDir.path}/osint_v_export.json');
    final data = {'people': people.map((p) => p.toJson()).toList()};
    await f.writeAsString(const JsonEncoder.withIndent('  ').convert(data));
    return f;
  }

  Future<void> importJsonFromFile(File f) async {
    final raw = await f.readAsString();
    final data = jsonDecode(raw) as Map<String, dynamic>;
    final imported = ((data['people'] as List?) ?? [])
        .map((e) => Person.fromJson(e))
        .toList();
    final byId = {for (final p in people) p.id: p};
    for (final p in imported) {
      byId[p.id] = p;
    }
    people = byId.values.toList();
    await persist();
  }

  Future<void> resetDatabase() async {
    people = [];
    await persist();
  }

  /// Export the in-memory data into a real SQLite database file.
  /// The app's working storage is JSON, but for portability the export is a
  /// proper `.db` file that can be opened by DB Browser for SQLite, the
  /// `sqlite3` CLI, etc.
  Future<File> exportRawDb() async {
    final outPath = '${docsDir.path}/osint_v_db.db';
    final outFile = File(outPath);
    if (await outFile.exists()) {
      try {
        await outFile.delete();
      } catch (_) {}
    }

    final db = await openDatabase(outPath, version: 1);
    try {
      final batch = db.batch();
      batch.execute('''
        CREATE TABLE people (
          id TEXT PRIMARY KEY,
          name TEXT NOT NULL,
          surname TEXT NOT NULL,
          patronymic TEXT NOT NULL,
          notes TEXT NOT NULL
        )
      ''');
      batch.execute('''
        CREATE TABLE person_tags (
          person_id TEXT NOT NULL,
          tag TEXT NOT NULL,
          PRIMARY KEY (person_id, tag)
        )
      ''');
      batch.execute('''
        CREATE TABLE categories (
          id TEXT PRIMARY KEY,
          person_id TEXT NOT NULL,
          ord INTEGER NOT NULL,
          name TEXT NOT NULL
        )
      ''');
      batch.execute('''
        CREATE TABLE kvs (
          id TEXT PRIMARY KEY,
          category_id TEXT NOT NULL,
          ord INTEGER NOT NULL,
          key TEXT NOT NULL,
          value TEXT NOT NULL
        )
      ''');
      batch.execute('''
        CREATE TABLE connections (
          id TEXT PRIMARY KEY,
          from_person_id TEXT NOT NULL,
          to_person_id TEXT NOT NULL
        )
      ''');
      batch.execute('''
        CREATE TABLE connection_reasons (
          connection_id TEXT NOT NULL,
          ord INTEGER NOT NULL,
          reason TEXT NOT NULL,
          PRIMARY KEY (connection_id, ord)
        )
      ''');
      batch.execute('''
        CREATE TABLE evidence (
          id TEXT PRIMARY KEY,
          person_id TEXT NOT NULL,
          description TEXT NOT NULL
        )
      ''');
      batch.execute('''
        CREATE TABLE evidence_files (
          evidence_id TEXT NOT NULL,
          ord INTEGER NOT NULL,
          path TEXT NOT NULL,
          PRIMARY KEY (evidence_id, ord)
        )
      ''');
      await batch.commit(noResult: true);

      final write = db.batch();
      for (final p in people) {
        write.insert('people', {
          'id': p.id,
          'name': p.name,
          'surname': p.surname,
          'patronymic': p.patronymic,
          'notes': p.notes,
        });
        for (final t in p.tags) {
          write.insert(
            'person_tags',
            {'person_id': p.id, 'tag': t},
            conflictAlgorithm: ConflictAlgorithm.ignore,
          );
        }
        for (var ci = 0; ci < p.categories.length; ci++) {
          final c = p.categories[ci];
          write.insert('categories', {
            'id': c.id,
            'person_id': p.id,
            'ord': ci,
            'name': c.name,
          });
          for (var ki = 0; ki < c.entries.length; ki++) {
            final kv = c.entries[ki];
            write.insert('kvs', {
              'id': kv.id,
              'category_id': c.id,
              'ord': ki,
              'key': kv.key,
              'value': kv.value,
            });
          }
        }
        for (final link in p.connections) {
          write.insert('connections', {
            'id': link.id,
            'from_person_id': p.id,
            'to_person_id': link.targetPersonId,
          });
          for (var ri = 0; ri < link.reasons.length; ri++) {
            write.insert('connection_reasons', {
              'connection_id': link.id,
              'ord': ri,
              'reason': link.reasons[ri],
            });
          }
        }
        for (final ev in p.evidence) {
          write.insert('evidence', {
            'id': ev.id,
            'person_id': p.id,
            'description': ev.description,
          });
          for (var fi = 0; fi < ev.filePaths.length; fi++) {
            write.insert('evidence_files', {
              'evidence_id': ev.id,
              'ord': fi,
              'path': ev.filePaths[fi],
            });
          }
        }
      }
      await write.commit(noResult: true);
    } finally {
      await db.close();
    }
    return outFile;
  }
}

// ============================================================================
// PARSERS / DETECTION
// ============================================================================

class ValueDetector {
  // Coordinates: e.g. "50.4501, 30.5234"
  static final RegExp coordRe = RegExp(
    r'(-?\d{1,3}(?:\.\d+)?)\s*,\s*(-?\d{1,3}(?:\.\d+)?)',
  );

  // Phone: contains "+" and at least 7 digits, allows spaces, dashes
  static final RegExp phoneRe = RegExp(
    r'(\+\d[\d\s\-\(\)]{6,}\d)',
  );

  // Card: 13-19 consecutive digits possibly with spaces in groups of 4
  static final RegExp cardRe = RegExp(
    r'(?:\d{4}[\s-]?){3,4}\d{1,4}',
  );

  static LatLng? extractCoord(String value) {
    final m = coordRe.firstMatch(value.trim());
    if (m == null) return null;
    final lat = double.tryParse(m.group(1)!);
    final lng = double.tryParse(m.group(2)!);
    if (lat == null || lng == null) return null;
    if (lat.abs() > 90 || lng.abs() > 180) return null;
    return LatLng(lat, lng);
  }

  static String? extractPhone(String value) {
    final m = phoneRe.firstMatch(value);
    return m?.group(1);
  }

  static String? extractCard(String value) {
    final m = cardRe.firstMatch(value);
    if (m == null) return null;
    final digits = m.group(0)!.replaceAll(RegExp(r'\s|-'), '');
    if (digits.length < 13 || digits.length > 19) return null;
    return digits;
  }

  static String? matchedCustomMark(String value) {
    final marks = AppState.instance.settings.marks;
    final v = value.trimLeft();
    for (final m in marks) {
      if (m.char.isNotEmpty && v.startsWith(m.char)) return m.char;
    }
    return null;
  }
}

// ============================================================================
// THEME
// ============================================================================

ThemeData buildTheme(AppTheme t) {
  final seed = const Color(0xFF6750A4);
  switch (t) {
    case AppTheme.light:
      return ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: seed, brightness: Brightness.light),
      );
    case AppTheme.dark:
      return ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: seed, brightness: Brightness.dark),
      );
    case AppTheme.amoled:
      final base = ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: seed, brightness: Brightness.dark),
      );
      return base.copyWith(
        scaffoldBackgroundColor: Colors.black,
        canvasColor: Colors.black,
        appBarTheme: const AppBarTheme(backgroundColor: Colors.black),
        cardTheme: CardThemeData(
          color: const Color(0xFF0A0A0A),
          surfaceTintColor: Colors.transparent,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(14),
            side: const BorderSide(color: Color(0xFF1F1F1F)),
          ),
        ),
        dialogTheme:
            const DialogThemeData(backgroundColor: Color(0xFF0A0A0A)),
        bottomSheetTheme: const BottomSheetThemeData(
          backgroundColor: Color(0xFF0A0A0A),
        ),
      );
  }
}

// ============================================================================
// APP ROOT
// ============================================================================

class OsintApp extends StatelessWidget {
  const OsintApp({super.key});
  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: AppState.instance,
      builder: (context, _) {
        return MaterialApp(
          title: 'OSINT V',
          debugShowCheckedModeBanner: false,
          theme: buildTheme(AppState.instance.settings.theme),
          home: const HomeScreen(),
        );
      },
    );
  }
}

// ============================================================================
// HOME SCREEN
// ============================================================================

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});
  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  String _query = '';

  List<Person> get _filtered {
    final q = _query.trim().toLowerCase();
    final all = AppState.instance.people;
    if (q.isEmpty) return all;
    return all.where((p) {
      for (final s in p.searchHaystack()) {
        if (s.toLowerCase().contains(q)) return true;
      }
      return false;
    }).toList();
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: AppState.instance,
      builder: (context, _) {
        final list = _filtered;
        return Scaffold(
          appBar: AppBar(
            title: Text(tr('app_title')),
            actions: [
              IconButton(
                tooltip: tr('graph'),
                icon: const Icon(Icons.hub_outlined),
                onPressed: () => Navigator.push(
                  context,
                  MaterialPageRoute(builder: (_) => const GraphScreen()),
                ),
              ),
              IconButton(
                tooltip: tr('all_targets_table'),
                icon: const Icon(Icons.table_chart_outlined),
                onPressed: () => Navigator.push(
                  context,
                  MaterialPageRoute(builder: (_) => const TableScreen()),
                ),
              ),
              IconButton(
                tooltip: tr('settings'),
                icon: const Icon(Icons.settings_outlined),
                onPressed: () => Navigator.push(
                  context,
                  MaterialPageRoute(builder: (_) => const SettingsScreen()),
                ),
              ),
            ],
          ),
          body: Column(
            children: [
              Padding(
                padding: const EdgeInsets.fromLTRB(12, 8, 12, 4),
                child: TextField(
                  decoration: InputDecoration(
                    prefixIcon: const Icon(Icons.search),
                    hintText: tr('deep_search'),
                    border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(14)),
                    isDense: true,
                  ),
                  onChanged: (v) => setState(() => _query = v),
                ),
              ),
              Expanded(
                child: list.isEmpty
                    ? Center(
                        child: Padding(
                        padding: const EdgeInsets.all(24),
                        child: Text(
                          _query.isEmpty ? tr('no_targets') : tr('no_results'),
                          textAlign: TextAlign.center,
                          style: const TextStyle(color: Colors.grey),
                        ),
                      ))
                    : ListView.builder(
                        padding: const EdgeInsets.fromLTRB(8, 4, 8, 96),
                        itemCount: list.length,
                        itemBuilder: (context, i) => _PersonCard(person: list[i]),
                      ),
              ),
            ],
          ),
          floatingActionButton: FloatingActionButton.extended(
            onPressed: () => _showAddDialog(context),
            icon: const Icon(Icons.person_add_alt_1),
            label: Text(tr('new_target')),
          ),
        );
      },
    );
  }

  Future<void> _showAddDialog(BuildContext context) async {
    final nameC = TextEditingController();
    final surC = TextEditingController();
    final patC = TextEditingController();
    final tagsC = TextEditingController();
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(tr('new_target')),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                  controller: nameC,
                  autofocus: true,
                  decoration: InputDecoration(labelText: tr('name'))),
              TextField(
                  controller: surC,
                  decoration: InputDecoration(labelText: tr('surname'))),
              TextField(
                  controller: patC,
                  decoration: InputDecoration(labelText: tr('patronymic'))),
              const SizedBox(height: 8),
              TextField(
                controller: tagsC,
                decoration: InputDecoration(
                  labelText: tr('tags'),
                  hintText: 'tag1, tag2',
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: Text(tr('cancel'))),
          FilledButton(
              onPressed: () => Navigator.pop(ctx, true),
              child: Text(tr('add'))),
        ],
      ),
    );
    if (ok == true) {
      final p = Person(
        name: nameC.text.trim(),
        surname: surC.text.trim(),
        patronymic: patC.text.trim(),
        tags: tagsC.text
            .split(',')
            .map((s) => s.trim())
            .where((s) => s.isNotEmpty)
            .toList(),
      );
      AppState.instance.people.add(p);
      await AppState.instance.persist();
    }
  }
}

class _PersonCard extends StatelessWidget {
  final Person person;
  const _PersonCard({required this.person});
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 6, horizontal: 4),
      child: InkWell(
        borderRadius: BorderRadius.circular(14),
        onTap: () => Navigator.push(
          context,
          MaterialPageRoute(builder: (_) => PersonScreen(personId: person.id)),
        ),
        onLongPress: () async {
          final ok = await showDeleteDialog(context);
          if (ok == true) {
            AppState.instance.people.removeWhere((p) => p.id == person.id);
            // also remove dangling connections
            for (final p in AppState.instance.people) {
              p.connections.removeWhere((c) => c.targetPersonId == person.id);
            }
            await AppState.instance.persist();
          }
        },
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Row(
            children: [
              CircleAvatar(
                radius: 26,
                backgroundColor: theme.colorScheme.primaryContainer,
                child: Text(
                  person.initials,
                  style: TextStyle(
                    color: theme.colorScheme.onPrimaryContainer,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      person.fullName,
                      style: const TextStyle(
                          fontSize: 16, fontWeight: FontWeight.w600),
                    ),
                    const SizedBox(height: 4),
                    Wrap(
                      spacing: 4,
                      runSpacing: 4,
                      children: [
                        for (final t in person.tags)
                          Chip(
                            label: Text(t),
                            visualDensity: VisualDensity.compact,
                            materialTapTargetSize:
                                MaterialTapTargetSize.shrinkWrap,
                          ),
                        if (person.connections.isNotEmpty)
                          Chip(
                            label: Text(
                                '${person.connections.length} ${tr('connections_count')}'),
                            visualDensity: VisualDensity.compact,
                            materialTapTargetSize:
                                MaterialTapTargetSize.shrinkWrap,
                          ),
                      ],
                    ),
                  ],
                ),
              ),
              const Icon(Icons.chevron_right),
            ],
          ),
        ),
      ),
    );
  }
}

// ============================================================================
// PERSON SCREEN (Tabs: Info / Connections / Evidence / Map)
// ============================================================================

class PersonScreen extends StatefulWidget {
  final String personId;
  const PersonScreen({super.key, required this.personId});
  @override
  State<PersonScreen> createState() => _PersonScreenState();
}

class _PersonScreenState extends State<PersonScreen> {
  Person? get _person => AppState.instance.findById(widget.personId);
  Person get person => _person!;

  @override
  Widget build(BuildContext context) {
    if (AppState.instance.findById(widget.personId) == null) {
      return const Scaffold(body: Center(child: Text('—')));
    }
    return ListenableBuilder(
      listenable: AppState.instance,
      builder: (context, _) {
        // Guard again inside builder in case person was deleted while open
        final p = AppState.instance.findById(widget.personId);
        if (p == null) {
          WidgetsBinding.instance.addPostFrameCallback((_) {
            if (mounted) Navigator.of(context).pop();
          });
          return const Scaffold(body: Center(child: CircularProgressIndicator()));
        }
        return DefaultTabController(
          length: 4,
          child: Scaffold(
            appBar: AppBar(
              title: Text(p.fullName),
              actions: [
                IconButton(
                  tooltip: tr('edit'),
                  icon: const Icon(Icons.edit_outlined),
                  onPressed: _editBasics,
                ),
                IconButton(
                  tooltip: tr('generate_pdf'),
                  icon: const Icon(Icons.picture_as_pdf_outlined),
                  onPressed: () => _openPdfFlow(),
                ),
                PopupMenuButton<String>(
                  onSelected: (v) async {
                    if (v == 'delete') {
                      final ok = await showDeleteDialog(context);
                      if (ok == true) {
                        AppState.instance.people
                            .removeWhere((pp) => pp.id == widget.personId);
                        for (final pp in AppState.instance.people) {
                          pp.connections.removeWhere(
                              (c) => c.targetPersonId == widget.personId);
                        }
                        await AppState.instance.persist();
                        if (!mounted) return;
                        Navigator.pop(context);
                      }
                    }
                  },
                  itemBuilder: (_) => [
                    PopupMenuItem(value: 'delete', child: Text(tr('delete'))),
                  ],
                ),
              ],
              bottom: TabBar(
                isScrollable: true,
                tabAlignment: TabAlignment.start,
                tabs: [
                  Tab(text: tr('info')),
                  Tab(text: tr('connections')),
                  Tab(text: tr('evidence')),
                  Tab(text: tr('map')),
                ],
              ),
            ),
            body: TabBarView(
              children: [
                _InfoTab(person: p, onChange: _persist),
                _ConnectionsTab(person: p, onChange: _persist),
                _EvidenceTab(person: p, onChange: _persist),
                _MapTab(person: p),
              ],
            ),
          ),
        );
      },
    );
  }

  Future<void> _persist() => AppState.instance.persist();

  Future<void> _editBasics() async {
    final p = _person;
    if (p == null) return;
    final nameC = TextEditingController(text: p.name);
    final surC = TextEditingController(text: p.surname);
    final patC = TextEditingController(text: p.patronymic);
    final notesC = TextEditingController(text: p.notes);
    final tagsC =
        TextEditingController(text: p.tags.join(', '));

    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(tr('edit_target')),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                  controller: nameC,
                  autofocus: true,
                  decoration: InputDecoration(labelText: tr('name'))),
              TextField(
                  controller: surC,
                  decoration: InputDecoration(labelText: tr('surname'))),
              TextField(
                  controller: patC,
                  decoration: InputDecoration(labelText: tr('patronymic'))),
              TextField(
                controller: notesC,
                decoration: InputDecoration(labelText: tr('notes')),
                maxLines: 3,
              ),
              TextField(
                controller: tagsC,
                decoration: InputDecoration(
                  labelText: tr('tags'),
                  hintText: 'tag1, tag2',
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: Text(tr('cancel'))),
          FilledButton(
              onPressed: () => Navigator.pop(ctx, true),
              child: Text(tr('save'))),
        ],
      ),
    );
    if (ok == true) {
      p.name = nameC.text.trim();
      p.surname = surC.text.trim();
      p.patronymic = patC.text.trim();
      p.notes = notesC.text.trim();
      p.tags = tagsC.text
          .split(',')
          .map((s) => s.trim())
          .where((s) => s.isNotEmpty)
          .toList();
      await _persist();
    }
  }

  Future<void> _openPdfFlow() async {
    bool withConnections = false;
    bool withEvidence = false;
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => StatefulBuilder(builder: (ctx, setS) {
        return AlertDialog(
          title: Text(tr('pdf_options')),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              SwitchListTile(
                title: Text(tr('include_connections')),
                value: withConnections,
                onChanged: (v) => setS(() => withConnections = v),
              ),
              SwitchListTile(
                title: Text(tr('include_evidence')),
                value: withEvidence,
                onChanged: (v) => setS(() => withEvidence = v),
              ),
            ],
          ),
          actions: [
            TextButton(
                onPressed: () => Navigator.pop(ctx, false),
                child: Text(tr('cancel'))),
            FilledButton(
                onPressed: () => Navigator.pop(ctx, true),
                child: Text(tr('preview'))),
          ],
        );
      }),
    );
    if (ok == true && mounted) {
      final p = _person;
      if (p == null) return;
      final bytes = await PdfBuilder.buildPersonPdf(
        p,
        withConnections: withConnections,
        withEvidence: withEvidence,
      );
      if (!mounted) return;
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => PdfPreviewScreen(
            bytes: bytes,
            person: p,
            withEvidence: withEvidence,
          ),
        ),
      );
    }
  }
}

// ============================================================================
// INFO TAB (categories with reorderable key-values; also reorder categories)
// ============================================================================

class _InfoTab extends StatefulWidget {
  final Person person;
  final Future<void> Function() onChange;
  const _InfoTab({required this.person, required this.onChange});
  @override
  State<_InfoTab> createState() => _InfoTabState();
}

class _InfoTabState extends State<_InfoTab> {
  @override
  Widget build(BuildContext context) {
    final p = widget.person;
    return ListView(
      padding: const EdgeInsets.fromLTRB(12, 12, 12, 96),
      children: [
        if (p.notes.isNotEmpty)
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Text(p.notes),
            ),
          ),
        const SizedBox(height: 8),
        ReorderableListView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          buildDefaultDragHandles: false,
          onReorder: (oldIdx, newIdx) async {
            setState(() {
              if (newIdx > oldIdx) newIdx -= 1;
              final c = p.categories.removeAt(oldIdx);
              p.categories.insert(newIdx, c);
            });
            await widget.onChange();
          },
          itemCount: p.categories.length,
          itemBuilder: (ctx, i) {
            final c = p.categories[i];
            return _CategoryCard(
              key: ValueKey(c.id),
              index: i,
              category: c,
              onChange: () async {
                setState(() {});
                await widget.onChange();
              },
              onDelete: () async {
                final ok = await showDeleteDialog(context);
                if (ok == true) {
                  setState(() => p.categories.removeAt(i));
                  await widget.onChange();
                }
              },
            );
          },
        ),
        if (p.categories.isEmpty)
          Padding(
            padding: const EdgeInsets.all(20),
            child: Center(
              child: Text(tr('no_categories_yet'),
                  style: const TextStyle(color: Colors.grey)),
            ),
          ),
        const SizedBox(height: 8),
        FilledButton.tonalIcon(
          onPressed: _addCategory,
          icon: const Icon(Icons.add),
          label: Text(tr('add_category')),
        ),
      ],
    );
  }

  Future<void> _addCategory() async {
    final c = TextEditingController();
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(tr('add_category')),
        content: TextField(
          controller: c,
          autofocus: true,
          decoration: InputDecoration(labelText: tr('category_name')),
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: Text(tr('cancel'))),
          FilledButton(
              onPressed: () => Navigator.pop(ctx, true),
              child: Text(tr('save'))),
        ],
      ),
    );
    if (ok == true && c.text.trim().isNotEmpty) {
      setState(() => widget.person.categories
          .add(CategoryBlock(name: c.text.trim())));
      await widget.onChange();
    }
  }
}

class _CategoryCard extends StatefulWidget {
  final int index;
  final CategoryBlock category;
  final Future<void> Function() onChange;
  final Future<void> Function() onDelete;
  const _CategoryCard({
    super.key,
    required this.index,
    required this.category,
    required this.onChange,
    required this.onDelete,
  });
  @override
  State<_CategoryCard> createState() => _CategoryCardState();
}

class _CategoryCardState extends State<_CategoryCard> {
  @override
  Widget build(BuildContext context) {
    final c = widget.category;
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 6),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(12, 8, 4, 0),
            child: Row(
              children: [
                ReorderableDragStartListener(
                  index: widget.index,
                  child: const Padding(
                    padding: EdgeInsets.symmetric(horizontal: 4),
                    child: Icon(Icons.drag_indicator, color: Colors.grey),
                  ),
                ),
                Expanded(
                  child: Text(
                    c.name.isEmpty ? '—' : c.name,
                    style: const TextStyle(
                        fontSize: 16, fontWeight: FontWeight.w600),
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.edit_outlined, size: 20),
                  onPressed: _renameCategory,
                ),
                IconButton(
                  icon: const Icon(Icons.delete_outline, size: 20),
                  onPressed: widget.onDelete,
                ),
              ],
            ),
          ),
          ReorderableListView.builder(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            buildDefaultDragHandles: false,
            onReorder: (oldIdx, newIdx) async {
              setState(() {
                if (newIdx > oldIdx) newIdx -= 1;
                final kv = c.entries.removeAt(oldIdx);
                c.entries.insert(newIdx, kv);
              });
              await widget.onChange();
            },
            itemCount: c.entries.length,
            itemBuilder: (ctx, i) {
              final kv = c.entries[i];
              return _KvTile(
                key: ValueKey(kv.id),
                index: i,
                kv: kv,
                onEdit: () => _editKv(kv),
                onDelete: () async {
                  final ok = await showDeleteDialog(context);
                  if (ok == true) {
                    setState(() => c.entries.removeAt(i));
                    await widget.onChange();
                  }
                },
              );
            },
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(8, 0, 8, 8),
            child: TextButton.icon(
              onPressed: _addKv,
              icon: const Icon(Icons.add, size: 18),
              label: Text(tr('add_kv')),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _renameCategory() async {
    final c = TextEditingController(text: widget.category.name);
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(tr('rename_category')),
        content: TextField(
            controller: c,
            decoration: InputDecoration(labelText: tr('category_name'))),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: Text(tr('cancel'))),
          FilledButton(
              onPressed: () => Navigator.pop(ctx, true),
              child: Text(tr('save'))),
        ],
      ),
    );
    if (ok == true) {
      setState(() => widget.category.name = c.text.trim());
      await widget.onChange();
    }
  }

  Future<void> _addKv() async {
    final kv = KeyValue();
    final added = await _kvDialog(kv);
    if (added) {
      setState(() => widget.category.entries.add(kv));
      await widget.onChange();
    }
  }

  Future<void> _editKv(KeyValue kv) async {
    final ok = await _kvDialog(kv);
    if (ok) {
      setState(() {});
      await widget.onChange();
    }
  }

  Future<bool> _kvDialog(KeyValue kv) async {
    final keyC = TextEditingController(text: kv.key);
    final valC = TextEditingController(text: kv.value);
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(tr('add_kv')),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                  controller: keyC,
                  decoration: InputDecoration(labelText: tr('key'))),
              TextField(
                controller: valC,
                decoration: InputDecoration(labelText: tr('value')),
                maxLines: null,
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: Text(tr('cancel'))),
          FilledButton(
              onPressed: () => Navigator.pop(ctx, true),
              child: Text(tr('save'))),
        ],
      ),
    );
    if (ok == true) {
      kv.key = keyC.text.trim();
      kv.value = valC.text;
      return true;
    }
    return false;
  }
}

class _KvTile extends StatelessWidget {
  final int index;
  final KeyValue kv;
  final VoidCallback onEdit;
  final VoidCallback onDelete;
  const _KvTile({
    super.key,
    required this.index,
    required this.kv,
    required this.onEdit,
    required this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    final coord = ValueDetector.extractCoord(kv.value);
    final phone = ValueDetector.extractPhone(kv.value);
    final card = ValueDetector.extractCard(kv.value);
    final mark = ValueDetector.matchedCustomMark(kv.value);

    final actions = <Widget>[];

    void addCopyBtn(String text, {String? label}) {
      actions.add(IconButton(
        tooltip: label == null ? tr('copy') : '${tr('copy')}: $label',
        icon: const Icon(Icons.copy, size: 18),
        onPressed: () async {
          await Clipboard.setData(ClipboardData(text: text));
          if (context.mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                  content: Text(tr('copied')),
                  duration: const Duration(seconds: 1)),
            );
          }
        },
      ));
    }

    if (coord != null) {
      actions.add(IconButton(
        tooltip: tr('open_on_map'),
        icon: const Icon(Icons.place_outlined, size: 20),
        onPressed: () => Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) => SingleMarkerMapScreen(
              point: coord,
              label: kv.key.isEmpty ? '${coord.latitude}, ${coord.longitude}' : kv.key,
            ),
          ),
        ),
      ));
      addCopyBtn('${coord.latitude}, ${coord.longitude}',
          label: 'coords');
    }
    if (phone != null) addCopyBtn(phone, label: 'phone');
    if (card != null) addCopyBtn(card, label: 'card');
    if (mark != null) addCopyBtn(kv.value, label: 'mark $mark');

    return ListTile(
      contentPadding: const EdgeInsets.fromLTRB(4, 0, 4, 0),
      dense: true,
      leading: ReorderableDragStartListener(
        index: index,
        child: const Padding(
          padding: EdgeInsets.only(left: 6),
          child: Icon(Icons.drag_indicator, size: 18, color: Colors.grey),
        ),
      ),
      title: Text(
        kv.key.isEmpty ? '—' : kv.key,
        style: const TextStyle(fontSize: 13, color: Colors.grey),
      ),
      subtitle: _ValueText(value: kv.value),
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          ...actions,
          IconButton(
              icon: const Icon(Icons.edit_outlined, size: 18),
              onPressed: onEdit),
          IconButton(
              icon: const Icon(Icons.delete_outline, size: 18),
              onPressed: onDelete),
        ],
      ),
    );
  }
}

class _ValueText extends StatelessWidget {
  final String value;
  const _ValueText({required this.value});
  @override
  Widget build(BuildContext context) {
    final mark = ValueDetector.matchedCustomMark(value);
    if (mark == null || value.isEmpty) {
      return Text(value, style: const TextStyle(fontSize: 15));
    }
    final v = value.trimLeft();
    final remaining = v.substring(mark.length);
    return RichText(
      text: TextSpan(
        style: DefaultTextStyle.of(context).style.copyWith(fontSize: 15),
        children: [
          TextSpan(
              text: mark, style: const TextStyle(color: Colors.grey)),
          TextSpan(text: remaining),
        ],
      ),
    );
  }
}

// ============================================================================
// CONNECTIONS TAB
// ============================================================================

class _ConnectionsTab extends StatefulWidget {
  final Person person;
  final Future<void> Function() onChange;
  const _ConnectionsTab({required this.person, required this.onChange});
  @override
  State<_ConnectionsTab> createState() => _ConnectionsTabState();
}

class _ConnectionsTabState extends State<_ConnectionsTab> {
  @override
  Widget build(BuildContext context) {
    final p = widget.person;
    return Stack(
      children: [
        if (p.connections.isEmpty)
          Center(
            child: Text(tr('no_connections_yet'),
                style: const TextStyle(color: Colors.grey)),
          )
        else
          ListView.builder(
            padding: const EdgeInsets.fromLTRB(12, 12, 12, 96),
            itemCount: p.connections.length,
            itemBuilder: (ctx, i) {
              final link = p.connections[i];
              final other = AppState.instance.findById(link.targetPersonId);
              return Card(
                child: ListTile(
                  leading: CircleAvatar(
                    child:
                        Text(other?.initials ?? '?', textAlign: TextAlign.center),
                  ),
                  title: Text(other?.fullName ?? '???'),
                  subtitle: link.reasons.isEmpty
                      ? Text(tr('reasons'))
                      : Text(link.reasons.join('\n')),
                  isThreeLine: link.reasons.length > 1,
                  onTap: () {
                    if (other != null) {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => PersonScreen(personId: other.id),
                        ),
                      );
                    }
                  },
                  trailing: PopupMenuButton<String>(
                    onSelected: (v) async {
                      if (v == 'edit') {
                        await _editLink(link);
                      } else if (v == 'delete') {
                        final ok = await showDeleteDialog(context);
                        if (ok == true) {
                          setState(() => p.connections.removeAt(i));
                          await widget.onChange();
                        }
                      }
                    },
                    itemBuilder: (_) => [
                      PopupMenuItem(value: 'edit', child: Text(tr('edit'))),
                      PopupMenuItem(value: 'delete', child: Text(tr('delete'))),
                    ],
                  ),
                ),
              );
            },
          ),
        Positioned(
          right: 16,
          bottom: 16,
          child: FloatingActionButton.extended(
            heroTag: 'connFab',
            onPressed: _addConnection,
            icon: const Icon(Icons.link),
            label: Text(tr('add_connection')),
          ),
        ),
      ],
    );
  }

  Future<void> _addConnection() async {
    final all = AppState.instance.people
        .where((p) => p.id != widget.person.id)
        .toList();
    if (all.isEmpty) return;
    final picked = await showDialog<Person>(
      context: context,
      builder: (ctx) => SimpleDialog(
        title: Text(tr('select_person')),
        children: [
          for (final p in all)
            SimpleDialogOption(
              onPressed: () => Navigator.pop(ctx, p),
              child: Text(p.fullName),
            ),
        ],
      ),
    );
    if (picked == null) return;
    final link = ConnectionLink(targetPersonId: picked.id);
    final ok = await _editLinkDialog(link, picked);
    if (ok) {
      setState(() => widget.person.connections.add(link));
      // Mirror connection on the other side if not already present
      final alreadyLinked = picked.connections
          .any((c) => c.targetPersonId == widget.person.id);
      if (!alreadyLinked) {
        picked.connections.add(ConnectionLink(
          targetPersonId: widget.person.id,
          reasons: List.of(link.reasons),
        ));
      }
      await widget.onChange();
    }
  }

  Future<void> _editLink(ConnectionLink link) async {
    final other = AppState.instance.findById(link.targetPersonId);
    if (other == null) return;
    final ok = await _editLinkDialog(link, other);
    if (ok) {
      setState(() {});
      await widget.onChange();
    }
  }

  Future<bool> _editLinkDialog(ConnectionLink link, Person other) async {
    final c = TextEditingController(text: link.reasons.join('\n'));
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(other.fullName),
        content: TextField(
          controller: c,
          maxLines: 5,
          decoration: InputDecoration(labelText: tr('reasons')),
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: Text(tr('cancel'))),
          FilledButton(
              onPressed: () => Navigator.pop(ctx, true),
              child: Text(tr('save'))),
        ],
      ),
    );
    if (ok == true) {
      link.reasons = c.text
          .split('\n')
          .map((s) => s.trim())
          .where((s) => s.isNotEmpty)
          .toList();
      return true;
    }
    return false;
  }
}

// ============================================================================
// EVIDENCE TAB
// ============================================================================

class _EvidenceTab extends StatefulWidget {
  final Person person;
  final Future<void> Function() onChange;
  const _EvidenceTab({required this.person, required this.onChange});
  @override
  State<_EvidenceTab> createState() => _EvidenceTabState();
}

class _EvidenceTabState extends State<_EvidenceTab> {
  @override
  Widget build(BuildContext context) {
    final p = widget.person;
    return Stack(
      children: [
        if (p.evidence.isEmpty)
          Center(
            child: Text(tr('no_evidence_yet'),
                style: const TextStyle(color: Colors.grey)),
          )
        else
          ListView.builder(
            padding: const EdgeInsets.fromLTRB(12, 12, 12, 96),
            itemCount: p.evidence.length,
            itemBuilder: (ctx, i) {
              final ev = p.evidence[i];
              return Card(
                child: Padding(
                  padding: const EdgeInsets.all(10),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Expanded(
                            child: Text(
                              ev.description.isEmpty ? '—' : ev.description,
                              style: const TextStyle(
                                  fontSize: 15, fontWeight: FontWeight.w600),
                            ),
                          ),
                          IconButton(
                              icon: const Icon(Icons.edit_outlined, size: 20),
                              onPressed: () => _editEvidence(ev)),
                          IconButton(
                            icon: const Icon(Icons.delete_outline, size: 20),
                            onPressed: () async {
                              final ok = await showDeleteDialog(context);
                              if (ok == true) {
                                setState(() => p.evidence.removeAt(i));
                                await widget.onChange();
                              }
                            },
                          ),
                        ],
                      ),
                      const SizedBox(height: 6),
                      Wrap(
                        spacing: 8,
                        runSpacing: 8,
                        children: [
                          for (final fp in ev.filePaths) _FileTile(path: fp),
                        ],
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        Positioned(
          right: 16,
          bottom: 16,
          child: FloatingActionButton.extended(
            heroTag: 'evidFab',
            onPressed: _addEvidence,
            icon: const Icon(Icons.add),
            label: Text(tr('add_evidence')),
          ),
        ),
      ],
    );
  }

  Future<void> _addEvidence() async {
    final ev = EvidenceItem();
    final ok = await _evidenceDialog(ev);
    if (ok) {
      setState(() => widget.person.evidence.add(ev));
      await widget.onChange();
    }
  }

  Future<void> _editEvidence(EvidenceItem ev) async {
    final ok = await _evidenceDialog(ev);
    if (ok) {
      setState(() {});
      await widget.onChange();
    }
  }

  Future<bool> _evidenceDialog(EvidenceItem ev) async {
    final descC = TextEditingController(text: ev.description);
    List<String> picked = List.of(ev.filePaths);
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => StatefulBuilder(builder: (ctx, setS) {
        return AlertDialog(
          title: Text(tr('add_evidence')),
          content: SizedBox(
            width: 420,
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  TextField(
                    controller: descC,
                    decoration: InputDecoration(labelText: tr('description')),
                    maxLines: 3,
                  ),
                  const SizedBox(height: 12),
                  Wrap(
                    spacing: 8,
                    runSpacing: 8,
                    children: [
                      for (final p in picked)
                        InputChip(
                          label: Text(p.split('/').last,
                              overflow: TextOverflow.ellipsis),
                          onDeleted: () => setS(() => picked.remove(p)),
                        ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  TextButton.icon(
                    icon: const Icon(Icons.attach_file),
                    label: Text(tr('pick_files')),
                    onPressed: () async {
                      final res = await FilePicker.platform.pickFiles(
                          allowMultiple: true, withData: false);
                      if (res != null) {
                        for (final f in res.files) {
                          if (f.path == null) continue;
                          final dest = await _copyToAppDocs(File(f.path!));
                          setS(() => picked.add(dest.path));
                        }
                      }
                    },
                  ),
                ],
              ),
            ),
          ),
          actions: [
            TextButton(
                onPressed: () => Navigator.pop(ctx, false),
                child: Text(tr('cancel'))),
            FilledButton(
                onPressed: () => Navigator.pop(ctx, true),
                child: Text(tr('save'))),
          ],
        );
      }),
    );
    if (ok == true) {
      ev.description = descC.text;
      ev.filePaths = picked;
      return true;
    }
    return false;
  }
}

Future<File> _copyToAppDocs(File src) async {
  final docs = AppState.instance.docsDir;
  final evDir = Directory('${docs.path}/evidence');
  if (!await evDir.exists()) await evDir.create(recursive: true);
  final base = src.path.split('/').last;
  final dest = File('${evDir.path}/${DateTime.now().millisecondsSinceEpoch}_$base');
  return src.copy(dest.path);
}

bool _isImagePath(String path) {
  final p = path.toLowerCase();
  return p.endsWith('.jpg') ||
      p.endsWith('.jpeg') ||
      p.endsWith('.png') ||
      p.endsWith('.webp') ||
      p.endsWith('.gif') ||
      p.endsWith('.bmp');
}

class _FileTile extends StatelessWidget {
  final String path;
  const _FileTile({required this.path});
  @override
  Widget build(BuildContext context) {
    final isImg = _isImagePath(path);
    return InkWell(
      onTap: () => OpenFilex.open(path),
      child: Container(
        width: 120,
        height: 120,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(10),
          border: Border.all(color: Colors.grey.withValues(alpha: 0.4)),
        ),
        clipBehavior: Clip.hardEdge,
        child: isImg
            ? Image.file(File(path), fit: BoxFit.cover,
                errorBuilder: (_, __, ___) => _fileIcon())
            : _fileIcon(name: path.split('/').last),
      ),
    );
  }

  Widget _fileIcon({String? name}) {
    return Padding(
      padding: const EdgeInsets.all(8),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.insert_drive_file_outlined, size: 36),
          const SizedBox(height: 6),
          if (name != null)
            Text(
              name,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              textAlign: TextAlign.center,
              style: const TextStyle(fontSize: 11),
            ),
        ],
      ),
    );
  }
}

// ============================================================================
// MAP TAB (per-person, all coordinates from kv values)
// ============================================================================

class _MapTab extends StatelessWidget {
  final Person person;
  const _MapTab({required this.person});

  List<({LatLng pt, String label})> _collect() {
    final out = <({LatLng pt, String label})>[];
    for (final c in person.categories) {
      for (final kv in c.entries) {
        final pt = ValueDetector.extractCoord(kv.value);
        if (pt != null) {
          final label = kv.key.isEmpty ? c.name : kv.key;
          out.add((pt: pt, label: label));
        }
      }
    }
    return out;
  }

  @override
  Widget build(BuildContext context) {
    final markers = _collect();
    if (markers.isEmpty) {
      return Center(
        child: Text(tr('no_target_marker'),
            style: const TextStyle(color: Colors.grey)),
      );
    }
    final center = markers.first.pt;
    return FlutterMap(
      options: MapOptions(initialCenter: center, initialZoom: 6),
      children: [
        TileLayer(
          urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
          userAgentPackageName: 'osint_v',
        ),
        MarkerLayer(
          markers: [
            for (final m in markers)
              Marker(
                point: m.pt,
                width: 160,
                height: 60,
                alignment: Alignment.topCenter,
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(Icons.location_on, color: Colors.red, size: 36),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                      decoration: BoxDecoration(
                        color: Colors.black.withValues(alpha: 0.7),
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: Text(m.label,
                          style: const TextStyle(color: Colors.white, fontSize: 11),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis),
                    ),
                  ],
                ),
              ),
          ],
        ),
      ],
    );
  }
}

class SingleMarkerMapScreen extends StatelessWidget {
  final LatLng point;
  final String label;
  const SingleMarkerMapScreen({super.key, required this.point, required this.label});
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(label),
        actions: [
          IconButton(
            icon: const Icon(Icons.copy),
            onPressed: () async {
              await Clipboard.setData(ClipboardData(
                  text: '${point.latitude}, ${point.longitude}'));
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text(tr('copied'))),
                );
              }
            },
          ),
          IconButton(
            icon: const Icon(Icons.open_in_new),
            onPressed: () async {
              final url = Uri.parse(
                  'https://www.openstreetmap.org/?mlat=${point.latitude}&mlon=${point.longitude}#map=15/${point.latitude}/${point.longitude}');
              await launchUrl(url, mode: LaunchMode.externalApplication);
            },
          ),
        ],
      ),
      body: FlutterMap(
        options: MapOptions(initialCenter: point, initialZoom: 14),
        children: [
          TileLayer(
            urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
            userAgentPackageName: 'osint_v',
          ),
          MarkerLayer(markers: [
            Marker(
              point: point,
              width: 60,
              height: 60,
              child: const Icon(Icons.location_on,
                  color: Colors.red, size: 48),
            ),
          ]),
        ],
      ),
    );
  }
}

// ============================================================================
// SETTINGS SCREEN
// ============================================================================

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});
  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  @override
  Widget build(BuildContext context) {
    final s = AppState.instance.settings;
    return Scaffold(
      appBar: AppBar(title: Text(tr('settings'))),
      body: ListView(
        padding: const EdgeInsets.all(8),
        children: [
          _section(tr('theme')),
          SegmentedButton<AppTheme>(
            segments: [
              ButtonSegment(value: AppTheme.light, label: Text(tr('light'))),
              ButtonSegment(value: AppTheme.dark, label: Text(tr('dark'))),
              ButtonSegment(value: AppTheme.amoled, label: Text(tr('amoled'))),
            ],
            selected: {s.theme},
            onSelectionChanged: (set) async {
              s.theme = set.first;
              await AppState.instance.persistSettingsOnly();
              setState(() {});
            },
          ),
          const SizedBox(height: 16),
          _section(tr('language')),
          SegmentedButton<String>(
            segments: const [
              ButtonSegment(value: 'en', label: Text('EN')),
              ButtonSegment(value: 'ru', label: Text('RU')),
            ],
            selected: {s.language},
            onSelectionChanged: (set) async {
              s.language = set.first;
              await AppState.instance.persistSettingsOnly();
              setState(() {});
            },
          ),
          const SizedBox(height: 16),
          _section(tr('storage_path')),
          Card(
            child: ListTile(
              title: Text(tr('storage_dir_info')),
              subtitle: Text(AppState.instance.dataFilePath),
              isThreeLine: true,
              leading: const Icon(Icons.folder_outlined),
            ),
          ),
          const SizedBox(height: 16),
          _section('JSON / DB'),
          Card(
            child: Column(
              children: [
                ListTile(
                  leading: const Icon(Icons.upload_file),
                  title: Text(tr('export_json')),
                  onTap: _exportJson,
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.download),
                  title: Text(tr('import_json')),
                  onTap: _importJson,
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.storage),
                  title: Text(tr('export_db')),
                  onTap: _exportRawDb,
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.delete_forever, color: Colors.red),
                  title: Text(tr('reset_db'),
                      style: const TextStyle(color: Colors.red)),
                  onTap: _resetDb,
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          _section(tr('custom_marks')),
          Card(
            child: ListTile(
              leading: const Icon(Icons.label_outline),
              title: Text(tr('custom_marks')),
              subtitle: Text(
                s.marks.isEmpty
                    ? tr('custom_marks_subtitle')
                    : '${s.marks.length} • ${tr('custom_marks_subtitle')}',
              ),
              trailing: const Icon(Icons.chevron_right),
              onTap: () async {
                await Navigator.push(
                  context,
                  MaterialPageRoute(builder: (_) => const MarksScreen()),
                );
                if (mounted) setState(() {});
              },
            ),
          ),
          const SizedBox(height: 16),
          _section(tr('tutorial')),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Text(tr('tutorial_text'),
                  style: const TextStyle(height: 1.4)),
            ),
          ),
          const SizedBox(height: 16),
          _section(tr('about')),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Text(tr('disclaimer')),
            ),
          ),
          const SizedBox(height: 32),
        ],
      ),
    );
  }

  Widget _section(String title) => Padding(
        padding: const EdgeInsets.fromLTRB(8, 12, 8, 6),
        child: Text(title,
            style: const TextStyle(
                fontSize: 13,
                color: Colors.grey,
                fontWeight: FontWeight.w600)),
      );

  Future<void> _exportJson() async {
    final f = await AppState.instance.exportJsonFile();
    await Share.shareXFiles([XFile(f.path)], text: 'OSINT V data');
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(tr('snack_exported'))),
      );
    }
  }

  Future<void> _exportRawDb() async {
    final f = await AppState.instance.exportRawDb();
    await Share.shareXFiles([XFile(f.path)], text: 'OSINT V raw DB');
  }

  Future<void> _importJson() async {
    final res = await FilePicker.platform.pickFiles(
        type: FileType.custom, allowedExtensions: ['json']);
    if (res == null || res.files.single.path == null) return;
    await AppState.instance.importJsonFromFile(File(res.files.single.path!));
    if (mounted) {
      setState(() {});
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(tr('snack_imported'))),
      );
    }
  }

  Future<void> _resetDb() async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(tr('reset_db')),
        content: Text(tr('reset_confirm')),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: Text(tr('cancel'))),
          FilledButton(
              style: FilledButton.styleFrom(backgroundColor: Colors.red),
              onPressed: () => Navigator.pop(ctx, true),
              child: Text(tr('delete'))),
        ],
      ),
    );
    if (ok == true) {
      await AppState.instance.resetDatabase();
      if (mounted) setState(() {});
    }
  }

}

// ============================================================================
// MARKS SCREEN — separate page so settings stays uncluttered
// ============================================================================

class MarksScreen extends StatefulWidget {
  const MarksScreen({super.key});
  @override
  State<MarksScreen> createState() => _MarksScreenState();
}

class _MarksScreenState extends State<MarksScreen> {
  @override
  Widget build(BuildContext context) {
    final marks = AppState.instance.settings.marks;
    return Scaffold(
      appBar: AppBar(title: Text(tr('custom_marks'))),
      body: marks.isEmpty
          ? Center(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Text(
                  tr('no_marks_yet'),
                  textAlign: TextAlign.center,
                  style: const TextStyle(color: Colors.grey),
                ),
              ),
            )
          : ListView.separated(
              padding: const EdgeInsets.fromLTRB(8, 8, 8, 96),
              itemCount: marks.length,
              separatorBuilder: (_, __) => const SizedBox(height: 4),
              itemBuilder: (ctx, i) {
                final m = marks[i];
                return Card(
                  margin: EdgeInsets.zero,
                  child: ListTile(
                    leading: CircleAvatar(child: Text(m.char)),
                    title: Text(m.label.isEmpty ? m.char : m.label),
                    subtitle: m.label.isEmpty ? null : Text(m.char),
                    trailing: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        IconButton(
                          icon: const Icon(Icons.edit_outlined),
                          onPressed: () => _editMark(i),
                        ),
                        IconButton(
                          icon: const Icon(Icons.delete_outline),
                          onPressed: () async {
                            final ok = await showDeleteDialog(context);
                            if (ok == true) {
                              setState(() => marks.removeAt(i));
                              await AppState.instance.persistSettingsOnly();
                            }
                          },
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _addMark,
        icon: const Icon(Icons.add),
        label: Text(tr('add_mark')),
      ),
    );
  }

  Future<void> _addMark() async {
    final mark = await _markDialog();
    if (mark != null) {
      AppState.instance.settings.marks.add(mark);
      await AppState.instance.persistSettingsOnly();
      if (mounted) setState(() {});
    }
  }

  Future<void> _editMark(int index) async {
    final existing = AppState.instance.settings.marks[index];
    final updated = await _markDialog(initial: existing);
    if (updated != null) {
      AppState.instance.settings.marks[index] = updated;
      await AppState.instance.persistSettingsOnly();
      if (mounted) setState(() {});
    }
  }

  Future<CustomMark?> _markDialog({CustomMark? initial}) async {
    final cChar = TextEditingController(text: initial?.char ?? '');
    final cLabel = TextEditingController(text: initial?.label ?? '');
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(tr('add_mark')),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: cChar,
              maxLength: 3,
              autofocus: true,
              decoration: InputDecoration(labelText: tr('mark_char')),
            ),
            TextField(
              controller: cLabel,
              decoration: InputDecoration(labelText: tr('mark_label')),
            ),
          ],
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: Text(tr('cancel'))),
          FilledButton(
              onPressed: () => Navigator.pop(ctx, true),
              child: Text(tr('save'))),
        ],
      ),
    );
    if (ok == true && cChar.text.isNotEmpty) {
      return CustomMark(char: cChar.text, label: cLabel.text);
    }
    return null;
  }
}

// ============================================================================
// TABLE SCREEN (export md/csv/excel/pdf)
// ============================================================================

class TableScreen extends StatelessWidget {
  const TableScreen({super.key});

  List<List<String>> _rows() {
    final rows = <List<String>>[
      ['Name', 'Surname', 'Patronymic', 'Tags', 'Categories', 'Connections', 'Evidence'],
    ];
    for (final p in AppState.instance.people) {
      rows.add([
        p.name,
        p.surname,
        p.patronymic,
        p.tags.join(';'),
        p.categories.map((c) => '${c.name}(${c.entries.length})').join(';'),
        p.connections
            .map((l) =>
                AppState.instance.findById(l.targetPersonId)?.fullName ?? '?')
            .join(';'),
        p.evidence.length.toString(),
      ]);
    }
    return rows;
  }

  @override
  Widget build(BuildContext context) {
    final rows = _rows();
    return Scaffold(
      appBar: AppBar(
        title: Text(tr('all_targets_table')),
        actions: [
          PopupMenuButton<String>(
            icon: const Icon(Icons.download),
            onSelected: (v) async {
              switch (v) {
                case 'csv':
                  await _exportCsv(rows);
                  break;
                case 'xlsx':
                  await _exportExcel(rows);
                  break;
                case 'md':
                  await _exportMd(rows);
                  break;
                case 'pdf':
                  await _exportPdf(rows);
                  break;
              }
            },
            itemBuilder: (_) => [
              PopupMenuItem(value: 'csv', child: Text(tr('export_csv'))),
              PopupMenuItem(value: 'xlsx', child: Text(tr('export_excel'))),
              PopupMenuItem(value: 'md', child: Text(tr('export_md'))),
              PopupMenuItem(value: 'pdf', child: Text(tr('export_pdf'))),
            ],
          ),
        ],
      ),
      body: SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        child: SingleChildScrollView(
          child: DataTable(
            columns: [
              for (final h in rows.first) DataColumn(label: Text(h)),
            ],
            rows: [
              for (final r in rows.skip(1))
                DataRow(cells: [for (final c in r) DataCell(Text(c))]),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _exportCsv(List<List<String>> rows) async {
    final csv = const ListToCsvConverter().convert(rows);
    final f = File('${AppState.instance.docsDir.path}/osint_v_table.csv');
    await f.writeAsString(csv);
    await Share.shareXFiles([XFile(f.path)]);
  }

  Future<void> _exportExcel(List<List<String>> rows) async {
    final excel = xls.Excel.createExcel();
    final sheet = excel['Targets'];
    for (final r in rows) {
      sheet.appendRow([for (final c in r) xls.TextCellValue(c)]);
    }
    excel.delete('Sheet1');
    final bytes = excel.save();
    if (bytes == null) return;
    final f = File('${AppState.instance.docsDir.path}/osint_v_table.xlsx');
    await f.writeAsBytes(bytes);
    await Share.shareXFiles([XFile(f.path)]);
  }

  Future<void> _exportMd(List<List<String>> rows) async {
    final sb = StringBuffer();
    sb.writeln('| ${rows.first.join(' | ')} |');
    sb.writeln('| ${rows.first.map((_) => '---').join(' | ')} |');
    for (final r in rows.skip(1)) {
      sb.writeln(
          '| ${r.map((c) => c.replaceAll('|', '\\|').replaceAll('\n', ' ')).join(' | ')} |');
    }
    final f = File('${AppState.instance.docsDir.path}/osint_v_table.md');
    await f.writeAsString(sb.toString());
    await Share.shareXFiles([XFile(f.path)]);
  }

  Future<void> _exportPdf(List<List<String>> rows) async {
    final doc = pw.Document();
    final baseFont = await PdfGoogleFonts.notoSansRegular();
    final boldFont = await PdfGoogleFonts.notoSansBold();
    doc.addPage(
      pw.MultiPage(
        pageFormat: PdfPageFormat.a4.landscape,
        theme: pw.ThemeData.withFont(base: baseFont, bold: boldFont),
        build: (ctx) => [
          pw.Text('OSINT V — Targets',
              style: pw.TextStyle(
                  font: boldFont, fontSize: 18, fontWeight: pw.FontWeight.bold)),
          pw.SizedBox(height: 12),
          pw.Table.fromTextArray(
            data: rows,
            cellStyle: pw.TextStyle(font: baseFont, fontSize: 9),
            headerStyle:
                pw.TextStyle(font: boldFont, fontSize: 10, fontWeight: pw.FontWeight.bold),
          ),
        ],
      ),
    );
    final bytes = await doc.save();
    final f = File('${AppState.instance.docsDir.path}/osint_v_table.pdf');
    await f.writeAsBytes(bytes);
    await Share.shareXFiles([XFile(f.path)]);
  }
}

// ============================================================================
// GRAPH SCREEN
// ============================================================================

class GraphScreen extends StatefulWidget {
  const GraphScreen({super.key});
  @override
  State<GraphScreen> createState() => _GraphScreenState();
}

class _GraphScreenState extends State<GraphScreen> {
  String? _reasonFilter;

  Set<String> _allReasons() {
    final set = <String>{};
    for (final p in AppState.instance.people) {
      for (final c in p.connections) {
        set.addAll(c.reasons);
      }
    }
    return set;
  }

  @override
  Widget build(BuildContext context) {
    final graph = Graph();
    final nodeMap = <String, Node>{};
    for (final p in AppState.instance.people) {
      final n = Node.Id(p.id);
      nodeMap[p.id] = n;
      graph.addNode(n);
    }
    // Dedupe edges: connections are mirrored on both sides, so adding both
    // directions creates duplicate edges that confuse the layout algorithm
    // and cause nodes to settle on top of each other.
    final addedEdges = <String>{};
    for (final p in AppState.instance.people) {
      for (final c in p.connections) {
        if (_reasonFilter != null &&
            !c.reasons.contains(_reasonFilter)) continue;
        final to = nodeMap[c.targetPersonId];
        if (to == null) continue;
        final ids = [p.id, c.targetPersonId]..sort();
        final key = ids.join('|');
        if (addedEdges.contains(key)) continue;
        addedEdges.add(key);
        graph.addEdge(nodeMap[p.id]!, to);
      }
    }

    // More iterations + isolated repulsion gives the Fruchterman–Reingold
    // layout enough room to actually separate overlapping nodes.
    final builder = FruchtermanReingoldAlgorithm(iterations: 2500);
    final reasons = _allReasons().toList()..sort();

    return Scaffold(
      appBar: AppBar(
        title: Text(tr('graph')),
        actions: [
          PopupMenuButton<String?>(
            icon: const Icon(Icons.filter_list),
            onSelected: (v) => setState(() => _reasonFilter = v),
            itemBuilder: (_) => [
              PopupMenuItem<String?>(
                  value: null, child: Text(tr('all_reasons'))),
              for (final r in reasons)
                PopupMenuItem<String?>(value: r, child: Text(r)),
            ],
          ),
        ],
      ),
      body: graph.nodeCount() == 0
          ? Center(
              child: Text(tr('no_connections_yet'),
                  style: const TextStyle(color: Colors.grey)),
            )
          : InteractiveViewer(
              constrained: false,
              boundaryMargin: const EdgeInsets.all(200),
              minScale: 0.1,
              maxScale: 4,
              child: GraphView(
                graph: graph,
                algorithm: builder,
                paint: Paint()
                  ..color = Theme.of(context).colorScheme.primary
                  ..strokeWidth = 1.4
                  ..style = PaintingStyle.stroke,
                builder: (Node node) {
                  final id = node.key!.value as String;
                  final p = AppState.instance.findById(id);
                  return GestureDetector(
                    onTap: () {
                      if (p != null) {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (_) => PersonScreen(personId: p.id),
                          ),
                        );
                      }
                    },
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 12, vertical: 8),
                      decoration: BoxDecoration(
                        color: Theme.of(context).colorScheme.primaryContainer,
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(
                            color: Theme.of(context).colorScheme.primary,
                            width: 1.4),
                      ),
                      child: Text(
                        p?.fullName ?? '?',
                        style: TextStyle(
                            color: Theme.of(context)
                                .colorScheme
                                .onPrimaryContainer),
                      ),
                    ),
                  );
                },
              ),
            ),
    );
  }
}

// ============================================================================
// PDF BUILDER + PREVIEW
// ============================================================================

class PdfBuilder {
  static Future<Uint8List> buildPersonPdf(
    Person p, {
    required bool withConnections,
    required bool withEvidence,
  }) async {
    final doc = pw.Document();

    // Load Noto Sans from Google Fonts — full Unicode/Cyrillic support
    final baseFont = await PdfGoogleFonts.notoSansRegular();
    final boldFont = await PdfGoogleFonts.notoSansBold();

    pw.TextStyle ts(double size, {bool bold = false}) => pw.TextStyle(
          font: bold ? boldFont : baseFont,
          fontSize: size,
        );

    final imageWidgets = <pw.Widget>[];
    if (withEvidence) {
      for (final ev in p.evidence) {
        for (final fp in ev.filePaths) {
          if (_isImagePath(fp)) {
            try {
              final bytes = await File(fp).readAsBytes();
              final img = pw.MemoryImage(bytes);
              imageWidgets.add(pw.Padding(
                padding: const pw.EdgeInsets.symmetric(vertical: 6),
                child: pw.Column(
                  crossAxisAlignment: pw.CrossAxisAlignment.start,
                  children: [
                    pw.Image(img, height: 220, fit: pw.BoxFit.contain),
                    pw.Text(fp.split('/').last, style: ts(9)),
                  ],
                ),
              ));
            } catch (_) {}
          }
        }
      }
    }

    doc.addPage(
      pw.MultiPage(
        pageFormat: PdfPageFormat.a4,
        theme: pw.ThemeData.withFont(base: baseFont, bold: boldFont),
        build: (ctx) {
          final widgets = <pw.Widget>[
            pw.Text(p.fullName, style: ts(22, bold: true)),
            pw.Divider(),
            pw.SizedBox(height: 8),
          ];

          if (p.tags.isNotEmpty) {
            widgets.add(pw.Text('Tags: ${p.tags.join(', ')}', style: ts(11)));
            widgets.add(pw.SizedBox(height: 6));
          }
          if (p.notes.isNotEmpty) {
            widgets.add(pw.Text(p.notes, style: ts(11)));
            widgets.add(pw.SizedBox(height: 12));
          }

          for (final c in p.categories) {
            widgets.add(pw.SizedBox(height: 8));
            widgets.add(pw.Text(c.name, style: ts(14, bold: true)));
            widgets.add(pw.SizedBox(height: 4));
            for (final kv in c.entries) {
              widgets.add(pw.Bullet(
                  text: '${kv.key}: ${kv.value}', style: ts(11)));
            }
          }

          if (withEvidence && p.evidence.isNotEmpty) {
            widgets.add(pw.SizedBox(height: 14));
            widgets.add(pw.Text(tr('evidence'), style: ts(14, bold: true)));
            for (final ev in p.evidence) {
              widgets.add(pw.SizedBox(height: 6));
              widgets.add(pw.Text(ev.description, style: ts(11)));
              for (final fp in ev.filePaths) {
                widgets.add(pw.Text('• ${fp.split('/').last}', style: ts(10)));
              }
            }
            if (imageWidgets.isNotEmpty) {
              widgets.add(pw.SizedBox(height: 8));
              widgets.addAll(imageWidgets);
            }
          }

          if (withConnections && p.connections.isNotEmpty) {
            widgets.add(pw.SizedBox(height: 14));
            widgets.add(pw.Text(tr('connections'), style: ts(14, bold: true)));
            for (final link in p.connections) {
              final other = AppState.instance.findById(link.targetPersonId);
              widgets.add(pw.SizedBox(height: 4));
              widgets.add(pw.Text(other?.fullName ?? '?',
                  style: ts(12, bold: true)));
              for (final r in link.reasons) {
                widgets.add(pw.Text('  - $r', style: ts(10)));
              }
            }
          }

          return widgets;
        },
      ),
    );

    return doc.save();
  }
}

class PdfPreviewScreen extends StatelessWidget {
  final Uint8List bytes;
  final Person person;
  final bool withEvidence;
  const PdfPreviewScreen({
    super.key,
    required this.bytes,
    required this.person,
    required this.withEvidence,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('PDF — ${person.fullName}'),
        actions: [
          IconButton(
            icon: const Icon(Icons.share),
            onPressed: () async {
              final f =
                  File('${AppState.instance.docsDir.path}/${person.id}.pdf');
              await f.writeAsBytes(bytes);
              final files = <XFile>[XFile(f.path)];
              if (withEvidence) {
                for (final ev in person.evidence) {
                  for (final fp in ev.filePaths) {
                    if (await File(fp).exists()) files.add(XFile(fp));
                  }
                }
              }
              await Share.shareXFiles(files);
            },
          ),
          IconButton(
            icon: const Icon(Icons.download),
            onPressed: () async {
              final f =
                  File('${AppState.instance.docsDir.path}/${person.id}.pdf');
              await f.writeAsBytes(bytes);
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Saved: ${f.path}')),
                );
              }
            },
          ),
        ],
      ),
      body: PdfPreview(
        build: (_) async => bytes,
        canChangePageFormat: false,
        canChangeOrientation: false,
        canDebug: false,
        allowPrinting: true,
        allowSharing: false,
      ),
    );
  }
}

// ============================================================================
// SHARED WIDGETS / DIALOGS
// ============================================================================

Future<bool?> showDeleteDialog(BuildContext context) {
  return showDialog<bool>(
    context: context,
    builder: (ctx) => AlertDialog(
      title: Text(tr('confirm_delete')),
      content: Text(tr('this_action_cannot_be_undone')),
      actions: [
        TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: Text(tr('cancel'))),
        FilledButton(
          style: FilledButton.styleFrom(backgroundColor: Colors.red),
          onPressed: () => Navigator.pop(ctx, true),
          child: Text(tr('delete')),
        ),
      ],
    ),
  );
}
