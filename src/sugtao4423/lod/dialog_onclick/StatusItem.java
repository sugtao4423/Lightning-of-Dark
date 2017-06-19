package sugtao4423.lod.dialog_onclick;

import java.io.Serializable;

public class StatusItem implements Serializable{

	private static final long serialVersionUID = 2282135656914830283L;

	private twitter4j.Status status;

	public StatusItem(twitter4j.Status status){
		this.status = status;
	}

	public twitter4j.Status getStatus(){
		return status;
	}
}
