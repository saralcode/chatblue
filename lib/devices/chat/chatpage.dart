import 'package:chatblue/utils/handler.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

class ChatPage extends StatefulWidget {
  const ChatPage({super.key});

  @override
  State<ChatPage> createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {
  final TextEditingController controller = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return GetBuilder<Handler>(builder: (handler) {
      return Scaffold(
        appBar: AppBar(
          title: const Text("Chat"),
        ),
        extendBody: true,
        body: ListView(
          reverse: true,
          controller: handler.scrollController,
          // mainAxisSize: MainAxisSize.max,
          // mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            const SizedBox(
              height: 100,
            ),
            ListView.builder(
                physics: const NeverScrollableScrollPhysics(),
                shrinkWrap: true,
                itemCount: handler.messages.length,
                padding: const EdgeInsets.all(10),
                itemBuilder: (context, index) {
                  Message message = handler.messages.elementAt(index);
                  return Column(
                    crossAxisAlignment: message.isSent
                        ? CrossAxisAlignment.end
                        : CrossAxisAlignment.start,
                    children: [
                      const SizedBox(
                        height: 5,
                      ),
                      Text(
                        message.name,
                        textScaleFactor: 1.1,
                      ),
                      Container(
                        margin: EdgeInsets.only(
                            left: message.isSent ? 40 : 0,
                            right: message.isSent ? 0 : 40),
                        child: Card(
                            color: message.isSent
                                ? Colors.pink.shade800
                                : Colors.blue.shade700,
                            shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(15)),
                            child: Padding(
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 15.0, vertical: 10),
                              child: Text(
                                message.text,
                                style: const TextStyle(color: Colors.white),
                                textScaleFactor: 1.1,
                              ),
                            )),
                      ),
                    ],
                  );
                }),
          ],
        ),
        bottomSheet: Padding(
          padding:
              const EdgeInsets.only(bottom: 40, left: 10, top: 10, right: 10),
          child: TextFormField(
            minLines: 1,
            maxLines: 4,
            keyboardType: TextInputType.multiline,
            controller: controller,
            onChanged: (v) {
              setState(() {});
            },
            decoration: inputDecoration(
                hintText: "Type your message...",
                suffixIcon: Padding(
                  padding: const EdgeInsets.only(right: 5, bottom: 5),
                  child: IconButton(
                      onPressed: () {
                        handler.sendMessage(controller.text);
                        controller.text = "";
                        setState(() {});
                      },
                      icon: Transform.rotate(
                        angle: -0.3,
                        child: Icon(Icons.send,
                            size: 30,
                            color: controller.text.isEmpty
                                ? Colors.grey.shade400
                                : Colors.blue),
                      )),
                )),
          ),
        ),
      );
    });
  }
}

OutlineInputBorder border({Color color = Colors.blue}) {
  return OutlineInputBorder(
    borderRadius: BorderRadius.circular(25),
    borderSide: BorderSide(width: 2, color: color),
  );
}

InputDecoration inputDecoration(
    {String? hintText,
    Icon? prefixIcon,
    Widget? suffixIcon,
    bool isRequired = false}) {
  return InputDecoration(
      border: border(),
      errorBorder: border(color: Colors.red),
      enabledBorder: border(),
      focusedBorder: border(color: Colors.blue.shade700),
      focusedErrorBorder: border(color: Colors.red),
      isDense: true,
      prefixIcon: prefixIcon,
      suffixIcon: suffixIcon,
      // labelText: hintText,
      constraints: const BoxConstraints.tightFor(width: 500),
      hintText: hintText);
}
