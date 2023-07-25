import 'package:chatblue/utils/handler.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bluetooth_serial/flutter_bluetooth_serial.dart';
import 'package:get/get.dart';

class Devices extends StatefulWidget {
  const Devices({super.key});

  @override
  State<Devices> createState() => _DevicesState();
}

class _DevicesState extends State<Devices> {
  @override
  void initState() {
    Handler handler = Get.find<Handler>();
    handler.getDevices();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () async {
        await FlutterBluetoothSerial.instance.cancelDiscovery();
        return true;
      },
      child: GetBuilder<Handler>(builder: (state) {
        return Scaffold(
          appBar: AppBar(
            title: const Text("Devices"),
          ),
          body: ListView(
            padding: const EdgeInsets.all(10),
            children: [
              const Text(
                "Paired Devices",
                textScaleFactor: 1.2,
              ),
              const Divider(),
              ListView.builder(
                  physics: const NeverScrollableScrollPhysics(),
                  shrinkWrap: true,
                  itemCount: state.devices.length,
                  itemBuilder: (context, index) {
                    var device = state.devices.elementAt(index);
                    String address = device['address'];
                    return Card(
                      child: ListTile(
                        onTap: () {
                          state.connect(address);
                        },
                        title: Text(device['name']),
                        subtitle: Text(address),
                      ),
                    );
                  }),
              const Text(
                "New Devices",
                textScaleFactor: 1.2,
              ),
              const Divider(),
              StreamBuilder(
                  stream: FlutterBluetoothSerial.instance.startDiscovery(),
                  builder: (context,
                      AsyncSnapshot<BluetoothDiscoveryResult> snapshot) {
                    if (snapshot.connectionState == ConnectionState.waiting) {
                      return const LinearProgressIndicator();
                    }
                    if (snapshot.data == null) {
                      return const Text("No Device Found");
                    }

                    BluetoothDevice device = snapshot.data!.device;
                    return Card(
                      child: ListTile(
                        onTap: () {
                          state.pair(device.address);
                        },
                        title: Text(device.name!),
                        subtitle: Text(device.address),
                      ),
                    );
                  }),
            ],
          ),
          // body: GetBuilder<Handler>(builder: (state) {
          //   return ListView.builder(
          //       itemCount: state.devices.length,
          //       itemBuilder: (context, index) {
          //         var device = state.devices.elementAt(index);
          //         String address = device['address'];
          //         return Card(
          //           child: ListTile(
          //             onTap: () {
          //               state.connect(address);
          //             },
          //             title: Text(device['name']),
          //             subtitle: Text(address),
          //             trailing: IconButton(
          //                 onPressed: () {
          //                   state.sendMessage("Hello");
          //                 },
          //                 icon: const Icon(Icons.message)),
          //           ),
          //         );
          //       });
          // }),
        );
      }),
    );
  }
}
