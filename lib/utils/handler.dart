// ignore_for_file: avoid_print

import 'dart:convert';
import 'dart:developer';

import 'package:chatblue/devices/chat/chatpage.dart';
import 'package:chatblue/home/homepage.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bluetooth_serial/flutter_bluetooth_serial.dart';
import 'package:get/get.dart';

class Message {
  String text;
  bool isSent;
  String name;
  Message({required this.text, required this.isSent, required this.name});
}

class Handler extends GetxController {
  final MethodChannel _methodChannel =
      const MethodChannel("com.example.chatblue");
  ScrollController scrollController = ScrollController();
  RxBool isConnected = false.obs;
  List devices = [].obs;
  RxBool isServerStarted = false.obs;
  RxBool isEnabled = false.obs;
  List<Message> messages = [];
  String bluetoothState = "Disabled";

  String name = "";
  Future<void> toogleBluetooth() async {
    Map<dynamic, dynamic> message = await _methodChannel.invokeMethod("toggle");
    log("${message['message']}");
  }

  void scrollToBottom() {
    scrollController.animateTo(
      0,
      curve: Curves.easeOut,
      duration: const Duration(milliseconds: 500),
    );
  }

  void disconnect(
      {bool isConnectedUser = false, bool isDisconnected = false}) async {
    if (!isDisconnected) {
      if (!isConnectedUser) {
        Map data = {
          "text": "",
          "user": name,
          "code": 0 // 1 = message, 0 = disconnect
        };
        await _methodChannel.invokeMethod("send-message", jsonEncode(data));
      }
      _methodChannel.invokeMethod("disconnect");
    }
    isConnected.value = false;
    messages.clear();
    isServerStarted.value = false;

    update();
    checkConnection();
    log(Get.currentRoute);
    Get.offAll(() => const HomePage());
  }

  @override
  void onInit() {
    super.onInit();
    _methodChannel.setMethodCallHandler((call) async {
      String data = call.arguments;
      log(call.method);
      switch (call.method) {
        case "connection":
          print("Connection ${call.arguments}");
          Get.showSnackbar(GetSnackBar(
            snackPosition: SnackPosition.TOP,
            duration: const Duration(seconds: 2),
            title: "Info",
            backgroundColor: Colors.green.shade500,
            messageText: Text(
              data,
              style: const TextStyle(
                  fontWeight: FontWeight.bold, color: Colors.white),
            ),
          ));
          if (data == "Connected") {
            isConnected.value = true;
            Get.to(() => const ChatPage());
          }
          update();
          break;
        case "btState":
          isEnabled.value = data == "10" ? false : true;
          switch (data) {
            case "1":
              bluetoothState = "WATING FOR DEVICE";
              break;
            case "2":
              bluetoothState = "CONNECTED";
              isConnected.value = true;
              Get.offAll(() => const HomePage());
              break;
            case "3":
              bluetoothState = "DISCONNECTING";
              break;
            case "4":
              bluetoothState = "Waiting for Connection";
              break;
            case "0":
              bluetoothState = "DISCONNECTED";
              disconnect(isDisconnected: true);
              break;
            case "11":
              bluetoothState = "TURNING ON";
              break;
            case "12":
              bluetoothState = "ON";
              break;
            case "10":
              bluetoothState = "OFF";
              disconnect();
              break;
            case "13":
              bluetoothState = "TURNING OFF";
              break;
          }
          update();
          print("Bluetooth State $data");
          // isEnabled.value = data == "0" ? false : true;
          break;
        case "message":
          Map decodedData = jsonDecode(data);

          if (decodedData['code'] == 0) {
            disconnect(isConnectedUser: true);
          } else {
            Message message = Message(
                text: decodedData['text'],
                isSent: false,
                name: decodedData['user']);
            log("$message");
            messages.add(message);
            update();
            scrollToBottom();
          }
          print("Message ${call.arguments}");

          break;
        default:
          print("Default called ${call.arguments}");
      }
    });
    checkConnection();
  }

  Future<void> pair(String address) async {
    String info = await _methodChannel.invokeMethod("pair", address);
    log(info);
  }

  Future<void> checkConnection() async {
    Map info = await _methodChannel.invokeMethod("getInfo");
    isEnabled.value = info['isEnabled'];
    bluetoothState = isEnabled.isTrue ? "ON" : "OFF";
    try {
      name = info['name'];
    } catch (e) {
      log("Error $e");
    }
    update();
  }

  Future<void> getDevices() async {
    devices = await _methodChannel.invokeMethod("getDevices");
    update();
    try {
      FlutterBluetoothSerial serial = FlutterBluetoothSerial.instance;
      serial.startDiscovery().listen((devices) {
        log("${devices.device.name}");
      });
    } catch (e) {
      log("hello");
    }
    log("$devices");
  }

  Future<void> connect(String address) async {
    _methodChannel.invokeMethod("disconnect");
    bool info = await _methodChannel.invokeMethod("connect-client", address);
    log("$info");
  }

  Future<void> startServer() async {
    bool info = await _methodChannel.invokeMethod("start-server");
    isServerStarted.value = true;
    log("$info");
  }

  Future<void> sendMessage(String message) async {
    if (isConnected.isTrue) {
      Map data = {
        "text": message,
        "user": name,
        "code": 1 // 1 = message, 0 = disconnect
      };
      String value = jsonEncode(data);
      bool info = await _methodChannel.invokeMethod("send-message", value);
      log("$info");
      Message m = Message(text: message, isSent: true, name: name);
      messages.add(m);
      update();
      scrollToBottom();
    }
  }
}
