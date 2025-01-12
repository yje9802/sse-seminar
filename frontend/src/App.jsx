import { useState } from "react";

const App = () => {
	const [notifications, setNotifications] = useState([]);
	let eventSource = null;

	// 알림 데이터 받는 부분을 버튼 클릭 시 처리
	const startSseSubscription = () => {
		// SSE 구독 시작
		eventSource = new EventSource("http://localhost:8080/notify/subscribe");

		// 연결 성공
		eventSource.onopen = () => {
			console.log("SSE connection opened");
		};

		// 알림 데이터를 받을 때 처리
		eventSource.addEventListener("sse-notification", async (event) => {
			console.log("Received SSE event:", event.data); // 데이터 수신 확인

			try {
				const notificationData = JSON.parse(event.data);
				setNotifications((prev) => [
					...prev,
					{
						title: notificationData.title,
						message: notificationData.content,
					},
				]);
			} catch (err) {
				console.error("Error parsing SSE data:", err);
			}
		});

		// 에러 발생 시 처리
		eventSource.onerror = (error) => {
			console.error("SSE connection error:", error);
			console.log("EventSource readyState:", eventSource.readyState);
			eventSource.close();
		};
	};

	// SSE 연결 종료
	const stopSseSubscription = () => {
		if (eventSource) {
			eventSource.close();
			console.log("SSE connection closed");
		}
	};

	// 알림 생성 요청
	const sendNotification = async () => {
		try {
			const response = await fetch(
				"http://localhost:8080/notify/send-notification",
				{ method: "POST" }
			);
			if (response.ok) {
				alert("Notification sent!");
			} else {
				alert("Failed to send notification.");
			}
		} catch (error) {
			console.error("Error sending notification:", error);
		}
	};

	return (
		<div style={{ fontFamily: "Arial, sans-serif", padding: "20px" }}>
			<h1>SSE Notification Test</h1>
			<button
				onClick={sendNotification}
				style={{
					padding: "10px 20px",
					backgroundColor: "#007bff",
					color: "#fff",
					border: "none",
					borderRadius: "5px",
					cursor: "pointer",
				}}
			>
				Send Notification
			</button>
			<button
				onClick={startSseSubscription}
				style={{
					padding: "10px 20px",
					backgroundColor: "#28a745",
					color: "#fff",
					border: "none",
					borderRadius: "5px",
					cursor: "pointer",
					marginLeft: "10px",
				}}
			>
				Start SSE Subscription
			</button>
			<button
				onClick={stopSseSubscription}
				style={{
					padding: "10px 20px",
					backgroundColor: "#dc3545",
					color: "#fff",
					border: "none",
					borderRadius: "5px",
					cursor: "pointer",
					marginLeft: "10px",
				}}
			>
				Stop SSE Subscription
			</button>
			<div id="notifications" style={{ marginTop: "20px" }}>
				{notifications.map((notification, index) => (
					<div
						key={index}
						style={{
							border: "1px solid #ddd",
							padding: "10px",
							marginBottom: "10px",
							borderRadius: "5px",
							backgroundColor: "#f9f9f9",
						}}
					>
						<div
							style={{
								fontWeight: "bold",
								fontSize: "16px",
								color: "#546de5",
							}}
						>
							Title: {notification.title}
						</div>
						<div style={{ marginTop: "5px", color: "#596275" }}>
							Message: {notification.message}
						</div>
					</div>
				))}
			</div>
		</div>
	);
};

export default App;
