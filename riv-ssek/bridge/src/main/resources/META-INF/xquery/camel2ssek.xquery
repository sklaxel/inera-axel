declare namespace soap = "http://schemas.xmlsoap.org/soap/envelope/";
declare namespace ns = "http://schemas.ssek.org/helloworld/2011-11-17";
declare namespace ssek = "http://schemas.ssek.org/ssek/2006-05-10/";

declare variable $in.headers.sender as xs:string external;
declare variable $in.headers.receiver as xs:string external;
declare variable $in.headers.txId as xs:string external;
declare variable $in.headers.payload as xs:string external;
declare variable $in.body as xs:string external;

<soap:Envelope>
    <soap:Header>
        <ssek:SSEK soap:mustUnderstand="1">
            <ssek:SenderId ssek:Type="CN">{$in.headers.sender}</ssek:SenderId>
            <ssek:ReceiverId ssek:Type="CN">{$in.headers.receiver}</ssek:ReceiverId>
            {
                if ($in.headers.txId)
                then <ssek:TxId>{$in.headers.txId}</ssek:TxId>
                else ()
            }
        </ssek:SSEK>
    </soap:Header>
    <soap:Body>
        {/soap:Envelope/soap:Body/*}
    </soap:Body>
</soap:Envelope>
