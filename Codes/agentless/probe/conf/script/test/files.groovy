import java.lang.*;
import com.broada.cid.action.protocol.impl.cli.*;
import com.broada.cid.action.spi.protocol.AbstractSession;
import com.broada.cid.common.api.error.ErrorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

executeMonitor();


def executeMonitor() {
	
	def ip = 127.0.0.1;
	def acInstances = $cli.execute('ll');
	
	result.addRow("test", [
	'age','25',
	'name','tom']);
	}