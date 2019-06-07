import React from 'react'

class InputCountry  extends React.Component {
    constructor(props) {
        super(props)
        this.state = { 
            country: 'australia',
            limit: '10000'
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
        this.props.handler(this.state.country, this.state.limit)
    }

    render() {
        return (
            <div class="form-group form-inline">
                <label class="col-form-label">Country:</label>
                <input 
                    value={this.state.country} 
                    onChange={evt => this.updateCountry(evt)}
                    class="form-control" type="text" name="country" id="inputCountry"/>
                <label class="col-form-label">Number of tracks:</label>
                <input 
                    value={this.state.limit} 
                    onChange={evt => this.updateLimit(evt)}
                    class="form-control" type="text" name="limit" id="inputLimit"/>
                <button 
                    value="tracks"
                    class="btn btn-primary"
                    onClick={() => this.submitCountryToHandler()}>Top Tracks</button>            
            </div>
        )
    }
}

export default InputCountry