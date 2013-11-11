declare namespace soapenv="http://schemas.xmlsoap.org/soap/envelope/";
declare namespace ns="http://schemas.ssek.org/helloworld/2011-11-17";
declare namespace ssek="http://schemas.ssek.org/ssek/";
declare namespace add="http://www.w3.org/2005/08/addressing";
declare namespace soap="http://schemas.xmlsoap.org/soap/";

declare variable $in.headers.SenderId as xs:string external; 
declare variable $in.headers.ReceiverId as xs:string external; 
declare variable $in.headers.TxId as xs:string external; 

<soapenv:Envelope>
   <soapenv:Header>
		<ssek:SSEK soap:mustUnderstand="1">
			<ssek:SenderId ssek:Type="CN">{$in.headers.SenderId}</ssek:SenderId>
			<ssek:ReceiverId ssek:Type="CN">{$in.headers.ReceiverId}</ssek:ReceiverId>
			<ssek:TxId>{$in.headers.TxId}</ssek:TxId>
		</ssek:SSEK>
   </soapenv:Header>
   <soapenv:Body>
	{/soapenv:Envelope/soapenv:Body/*}
   </soapenv:Body>
</soapenv:Envelope>
