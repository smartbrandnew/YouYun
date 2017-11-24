import java.lang.*;
import com.broada.cid.action.protocol.impl.cli.*;
import com.broada.cid.action.spi.protocol.AbstractSession;
import com.broada.cid.common.api.error.ErrorUtil;

executeMonitor();

def executeMonitor() {

	$cli.connect();
	$cli.execute("ps -ef | grep pmon | grep '+ASM'");
	String retCode = $cli.execute("echo $?");
	$cli.disconnect();
	int exist = 0;
	if(Integer.valueOf(retCode).intValue() == 0){
		exist = 1;	
	}
	result.addRow('asm', ['oracle.asm.status', exist]);
	
}