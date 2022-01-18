function vote(id, action, target) {
    const token = $('#_csrf').attr('content');
    const header = $('#_csrf_header').attr('content');

    const headers = {};
    headers[header] = token;
    header['Content-Type'] = 'application/json';

    fetch("/debts/" + id + "/" + action, {
        method: "POST",
        headers: headers
    }).then(response => {
        if (response.status !== 200) {
            console.log("Something went wrong", response)
        } else {
            if (action === 'vote') {
                target.classList.remove('bi-caret-up')
                target.classList.add('bi-caret-up-fill')
                target.setAttribute('data-action', 'downVote')
            } else {
                target.classList.remove('bi-caret-up-fill')
                target.classList.add('bi-caret-up')
                target.setAttribute('data-action', 'vote')
            }
        }
    });
}