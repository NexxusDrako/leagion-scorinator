public class TQueue 
{
  TQueueItem first, last;
  public boolean changed;

  TQueue()
  {
    first=null;
    last=null;
    changed=false;
  }

  public void enqueue(Object data)
  {
    TQueueItem temp;

    temp = new TQueueItem(data);
    if (last!=null)
    {
      last.next=temp;
      last=temp;
    }else
    {
      last=temp;
      first=temp;
    }
    changed=true;
  }

  public void dequeue()
  {
    first=first.next;
    if (first==null)
    {
      last=null;
    }
    changed=true;
  }

  public Object front()
  {
    if (first!=null)
    {
      return(first.data);
    }else
    {
      return(null);
    }
  }
  
  public boolean empty()
  {
    return first==null;
  }
}

class TQueueItem
{
  Object data;
  TQueueItem next;

  TQueueItem(Object _data)
  {
    data=_data;
    next=null;
  }
}