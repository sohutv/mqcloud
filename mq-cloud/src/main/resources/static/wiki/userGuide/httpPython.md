由于[rocketmq-client-python](https://github.com/apache/rocketmq-client-python)已经不再维护，而且存在某些bug，故不再推荐使用。

这里提供了python基于http协议的使用方式：

**<span id="produce">一</span>、生产消息代码示例如下：** 

```
# coding=utf-8
import requests
import time

def sendMessage():
	try:
		# 定义生产消息的参数
		payload = {'producer': 'mqcloud-http-test-topic-producer', 'message': '{"id":87312, "name":"abcd"}'}
		# 发送消息生产请求
		response = requests.post('http://${httpProducerUriPrefix}/mq/produce', data=payload)
		# 解析响应结果
		if response.status_code == 200:
			data = response.json()
			print(data)
		else:
			print('response errror', response.status_code)
			# 重试发送
			time.sleep(1)
			sendMessage()

	except Exception as e:
		print('reqeust error', e)
		# 重试发送
		time.sleep(3)
		sendMessage()

# 发送消息
sendMessage()
```

**<span id="consume">二</span>、消费消息代码示例如下：**

```
# coding=utf-8
import requests
import time

def httpConsume():
	# 定义消费消息的参数
	payload = {'topic': 'mqcloud-http-test-topic', 'consumer': 'clustering-mqcloud-http-consumer'}
	while True:
		try:
			# 拉取消息
			response = requests.post('http://${httpConsumerUriPrefix}/mq/message', data=payload)
			# 解析响应结果
			if response.status_code == 200:
				data = response.json()
				# 响应结果正常才解析消息
				if data['status'] == 200:
					# 更新requestId
					payload['requestId'] = data['result'].get('requestId', "")
					# 获取正常消息列表
					if data['result']['msgListSize'] > 0:
						for msg in data['result']['msgList']:
							print(msg['message'])

					# 获取重试消息列表
					if data['result']['retryMsgListSize'] > 0:
						for msg in data['result']['retryMsgList']:
							print(msg['message'])
				else:
					print('response errror', data)
			else:
				print('response status errror', response.status_code)

		except Exception as e:
			print('reqeust error', e)

		# 延时一会
		time.sleep(2)

# 消费消息
httpConsume()
```

关于http协议请求参数和响应数据说明请参考[http接入](http)。 