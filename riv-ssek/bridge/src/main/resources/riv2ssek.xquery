declare namespace soap="http://schemas.xmlsoap.org/soap/envelope/";
declare namespace ns="http://schemas.ssek.org/helloworld/2011-11-17";
declare namespace ssek="http://schemas.ssek.org/ssek/2006-05-10/";

declare variable $in.headers.senderId as xs:string external; 
declare variable $in.headers.receiverId as xs:string external; 
declare variable $in.headers.txId as xs:string external; 
declare variable $in.headers.payload as xs:string external; 

<soap:Envelope>
	<soap:Header>
		<ssek:SSEK soap:mustUnderstand="1">
			<ssek:SenderId ssek:Type="CN">{$in.headers.senderId}</ssek:SenderId>
			<ssek:ReceiverId ssek:Type="CN">{$in.headers.receiverId}</ssek:ReceiverId>
			<ssek:TxId>{$in.headers.txId}</ssek:TxId>
		</ssek:SSEK>
	</soap:Header>
	<soap:Body>
		<ns:HelloWorldRequest>
			<ns:Message>{$in.headers.payload}</ns:Message>
		</ns:HelloWorldRequest>
	</soap:Body>
</soap:Envelope>
