import React from 'react'

class InputCountry  extends React.Component {
    constructor(props) {
        super(props)
        this.state = { 
            country: 'australia',
            limit: '5000'
        } 
    }

    updateCountry(evt) {
        this.setState({
            country: evt.target.value,
            limit: this.state.limit
        })
    }

    updateLimit(evt) {
        this.setState({
            country: this.state.country,
            limit: evt.target.value
        })
    }

    submitCountryToHandler() {
        this.props.tracksHandler(this.state.country, this.state.limit)
    }

    clearcacheHandler(country) {
        this.props.cancelHandler()
        fetch('/api/clearcache', {
            method: 'post',
            headers: {
                "Content-type": "application/x-www-form-urlencoded; charset=UTF-8"
            },
            body: `country=${country}`
        })
        .catch(err => {
            alert(err.message)  
        })
    }

    render() {
        return (
            <div class="form-inline">
                <div class="form-group">
                    <label class="col-form-label">Country:</label>
                    <input 
                        value={this.state.country} 
                        onChange={evt => this.updateCountry(evt)}
                        class="form-control" type="text" name="country" id="inputCountry"/>
                    <label class="col-form-label">Limit:</label>
                    <input 
                        value={this.state.limit} 
                        onChange={evt => this.updateLimit(evt)}
                        class="form-control" type="text" name="limit" id="inputLimit"/>
                </div>
                <button 
                    value="tracks"
                    class="btn btn-primary"
                    onClick={() => this.submitCountryToHandler()}>Top Tracks</button>
                <button 
                    value="cancel"
                    class="btn btn-primary"
                    onClick={() => this.props.cancelHandler()}>CANCEL</button>
                <button 
                    value="clearcache"
                    class="btn btn-primary"
                    onClick={() => this.clearcacheHandler(this.state.country)}
                >
                        Clear Cache for {this.state.country}
                </button>
            </div>
        )
    }
}

export default InputCountry