import 'package:chatblue/devices/chat/chatpage.dart';
import 'package:chatblue/devices/devices.dart';
import 'package:chatblue/utils/handler.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:permission_handler/permission_handler.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  Future<void> getPermission() async {
    await Permission.bluetooth.request();
    // PermissionStatus status =
    await Permission.bluetoothConnect.request();

    await Permission.bluetoothScan.request();
    // log("${status}");
    await Permission.bluetoothAdvertise.request();
  }

  @override
  void initState() {
    getPermission();
    Get.put(Handler());
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Bluetooth"),
      ),
      body: GetBuilder<Handler>(builder: (handler) {
        return Center(
          child: ListView(
            padding: const EdgeInsets.all(10),
            children: [
              const SizedBox(
                height: 20,
              ),
              if (handler.name.isNotEmpty) ...[
                Text(
                  "Device : ${handler.name}",
                  textScaleFactor: 1.5,
                  style: const TextStyle(
                      fontWeight: FontWeight.bold,
                      color: Colors.deepOrangeAccent),
                  textAlign: TextAlign.center,
                ),
                const Divider(),
              ],
              const SizedBox(
                height: 30,
              ),
              const Text(
                "Bluetooth Status",
                textScaleFactor: 1.4,
                style:
                    TextStyle(fontWeight: FontWeight.bold, color: Colors.blue),
                textAlign: TextAlign.center,
              ),
              const SizedBox(
                height: 10,
              ),
              Text(
                handler.bluetoothState,
                textScaleFactor: 1.5,
                style: const TextStyle(
                    fontWeight: FontWeight.bold, color: Colors.purple),
                textAlign: TextAlign.center,
              ),
              const Divider(),
              const SizedBox(
                height: 30,
              ),
              if (handler.isConnected.isTrue) ...[
                FloatingActionButton.extended(
                  heroTag: "Chat",
                  onPressed: () {
                    Get.to(() => const ChatPage());
                  },
                  label: const Text("Chat"),
                  icon: const Icon(Icons.chat),
                ),
                const SizedBox(
                  height: 20,
                ),
                FloatingActionButton.extended(
                  heroTag: "disconnect",
                  backgroundColor: Colors.pink.shade700,
                  onPressed: () {
                    handler.disconnect();
                  },
                  label: const Text("Disconnect"),
                  icon: const Icon(Icons.stop),
                ),
              ] else ...[
                if (handler.isEnabled.isFalse) ...[
                  FloatingActionButton.extended(
                      heroTag: "bluetoothOn",
                      onPressed: () {
                        handler.toogleBluetooth();
                      },
                      icon: const Icon(Icons.bluetooth),
                      label: const Text("Turn ON Bluetooth")),
                ] else ...[
                  const SizedBox(
                    height: 20,
                  ),
                  if (handler.isServerStarted.isTrue)
                    FloatingActionButton.extended(
                      backgroundColor: Colors.pink.shade700,
                      heroTag: "stopserver",
                      onPressed: () {
                        handler.disconnect();
                      },
                      icon: const Icon(Icons.stop),
                      label: const Text("Stop Server"),
                    )
                  else
                    FloatingActionButton.extended(
                      backgroundColor: Colors.green,
                      heroTag: "server",
                      onPressed: () {
                        handler.startServer();
                      },
                      icon: const Icon(Icons.start),
                      label: const Text("Start Server"),
                    ),
                  const SizedBox(
                    height: 20,
                  ),
                  const Text(
                    "OR",
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(
                    height: 20,
                  ),
                  FloatingActionButton.extended(
                    heroTag: "pairedConnect",
                    onPressed: () {
                      Get.to(() => const Devices());
                    },
                    label: const Text("Connect to a Device"),
                  ),
                  const SizedBox(
                    height: 20,
                  ),
                ]
              ]
            ],
          ),
        );
      }),
    );
  }
}
