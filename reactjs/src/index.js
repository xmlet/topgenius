import React from 'react'
import ReactDOM from 'react-dom'
import ReactLoading from 'react-loading'
import InputCountry from './InputCountry'
import GeographicTopTracks from './GeographicTopTracks'

class App extends React.Component {
    constructor(props) {
        super(props)
        this.state = { 
            'fetching': false,
            'env': null
        }
        this.geoTopTracks = React.createRef();
    }

    componentWillMount() {
        fetch('/env')
            .then(resp => resp.json())
            .then(data => this.setState({
                'fetching': this.state.fetching,
                'env': data.env
            }))
    }

    tracksHandler(country, limit) {
        this.geoTopTracks.current.tracksHandler(country, limit)
    }

    render() {
        if (!this.state.env) {
            return <ReactLoading type={"bars"} color="#666666" />
        }
        return (
            <div>
                <div class="jumbotron">
                    <p class="lead">TopGenius.eu</p>
                    <hr/>
                    <InputCountry handler={(country, limit) => this.tracksHandler(country, limit)}/>
                    <div>
                        {this.state.fetching && <ReactLoading type={"bars"} color="#666666" /> }
                    </div>
                </div>
                <br />
                <GeographicTopTracks 
                    ref={this.geoTopTracks}
                    env={this.state.env}
                    updateFetching={(fetching => this.setState({ 'fetching': fetching }))} 
                />
            </div>
        )
    }
}

// ========================================

ReactDOM.render(
    <App />,
    document.getElementById('root')
);

